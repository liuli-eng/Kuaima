package com.kuaima.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.constant.BossType;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.service.BossOrderService;
import com.kuaima.app.domain.message.constant.MessageType;
import com.kuaima.app.domain.message.repository.MessageRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.constant.WithDrawStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.entity.Wallet;
import com.kuaima.app.domain.wallet.entity.WalletFlow;
import com.kuaima.app.domain.wallet.entity.WithDraw;
import com.kuaima.app.domain.wallet.service.SettlementService;
import com.kuaima.app.domain.wallet.service.WalletService;

/**
 * 全链路自测：发布招工 -> 报名 -> 录用 -> 到岗 -> 完工 -> 结算支付 -> 零工提现。
 * 连接本地 MySQL，ddl-auto=update 会在首次运行时自动创建新表；
 * 每个测试方法运行在事务中，结束后整体回滚，不污染数据库。
 * 金额单位均为分。
 */
@SpringBootTest
@Transactional
class RecruitmentAndWalletFlowTests {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BossOrderService bossOrderService;
    @Autowired
    private SettlementService settlementService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private MessageRepository messageRepository;

    private static final long WAGE_CENT = 30_000L; // 300元/天 x 1天

    /** 该用户收件箱里是否已收到指定类型消息（站内消息中心） */
    private boolean hasMessage(Long userId, String type) {
        return messageRepository.findByUserIdOrderByIdDesc(userId).stream()
                .anyMatch(m -> type.equals(m.getType()));
    }

    /** 创建测试用户（随机 username，避免唯一约束冲突） */
    private User createUser(String role) {
        User user = new User();
        user.setUsername("test_" + UUID.randomUUID().toString().substring(0, 8));
        user.setPassword("no-password");
        user.setRole(role);
        return userRepository.save(user);
    }

    /** 创建一个日结订单：300 元/天 */
    private BossOrder createDailyOrder(User boss) {
        BossOrder order = new BossOrder();
        order.setOrderTitle("日结搬运工");
        order.setType(BossType.DAILY);
        order.setPostion("搬运工");
        order.setOrderNum(5);
        order.setDuration(8);
        order.setSalary(300);
        order.setStartTime(new Date());
        order.setEndTime(new Date(System.currentTimeMillis() + 86_400_000L));
        BossOrder saved = bossOrderService.createOrder(order);
        assertNotNull(saved.getId());
        return saved;
    }

    @Test
    void fullFlow_publish_apply_settle_withdraw() {
        User boss = createUser(UserRole.BOSS);
        User worker = createUser(UserRole.USER);

        // 1. 老板发布订单（初始：招工中）；招聘广播应推送至员工收件箱
        BossOrder order = createDailyOrder(boss);
        assertEquals(BossStatus.ORDER_RECRUITING, order.getOrderStatus());
        assertTrue(hasMessage(worker.getId(), MessageType.ORDER_PUBLISH));

        // 2. 员工报名
        BaseOrderItem applied = bossOrderService.applyOrder(order.getId(), worker.getId(), "能吃苦", false);
        assertEquals(BossStatus.ITEM_APPLIED, applied.getStatus());

        // 3. 老板录用 -> 员工到岗 -> 完工（同日，自动推导 1 天）
        BaseOrderItem hired = bossOrderService.hireItem(applied.getId());
        assertEquals(BossStatus.ITEM_HIRED, hired.getStatus());
        assertTrue(hasMessage(worker.getId(), MessageType.ORDER_HIRE));
        BaseOrderItem onWork = bossOrderService.confirmWork(hired.getId());
        assertEquals(BossStatus.ITEM_ON_WORK, onWork.getStatus());
        BaseOrderItem finished = bossOrderService.finishItem(onWork.getId());
        assertEquals(BossStatus.ITEM_FINISHED, finished.getStatus());

        // 4. 老板发起结算（workDays 缺省，按到岗~完成自动推导 = 1 天）
        Settlement settlement = settlementService.createSettlement(finished.getId(), null);
        assertEquals(SettlementStatus.PENDING, settlement.getStatus());
        assertEquals(1, settlement.getWorkDays());
        assertEquals(WAGE_CENT, settlement.getWage());
        assertEquals(0L, settlement.getServiceFee());
        assertEquals(WAGE_CENT, settlement.getTotalAmount());

        // 5. 模拟支付 -> 工资入零工钱包 + 到账消息
        Settlement paid = settlementService.mockPay(settlement.getId());
        assertEquals(SettlementStatus.PAID, paid.getStatus());
        assertNotNull(paid.getPayNo());
        assertNotNull(paid.getPayTime());
        assertTrue(hasMessage(worker.getId(), MessageType.SETTLE_PAID));

        Wallet wallet = walletService.getOrCreateWallet(worker.getId());
        assertEquals(WAGE_CENT, wallet.getBalance());

        // 6. 钱包流水：一条工资入账
        List<WalletFlow> flows = walletService.listFlows(worker.getId());
        assertEquals(1, flows.size());
        assertEquals(WalletService.DIR_INCOME, flows.get(0).getDirection());
        assertEquals(WalletService.BIZ_WAGE, flows.get(0).getBizType());
        assertEquals(WAGE_CENT, flows.get(0).getAmount());
        assertEquals(WAGE_CENT, flows.get(0).getBalanceAfter());

        // 7. 零工提现 200 元(20000分)，余额冻结扣除
        WithDraw draw = walletService.applyWithdraw(worker.getId(), 20_000L, "mock-openid", "");
        assertEquals(WithDrawStatus.PENDING, draw.getStatus());
        assertEquals(20_000L, draw.getAmount());
        assertEquals(WAGE_CENT - 20_000L, walletService.getWallet(worker.getId()).getBalance());

        // 8. 模拟打款成功
        WithDraw paidDraw = walletService.mockPayout(draw.getId());
        assertEquals(WithDrawStatus.SUCCESS, paidDraw.getStatus());
        assertNotNull(paidDraw.getPayTime());

        // 9. 提现记录列表
        List<WithDraw> withdraws = walletService.listWithdraws(worker.getId());
        assertEquals(1, withdraws.size());
        assertEquals(WithDrawStatus.SUCCESS, withdraws.get(0).getStatus());

        // 10. 结算单列表（老板侧/零工侧均可见）
        assertEquals(1, settlementService.listByOrder(order.getId()).size());
        assertEquals(1, settlementService.listByWorker(worker.getId()).size());
    }

    @Test
    void withdrawFail_shouldRefundBalance() {
        User worker = createUser(UserRole.USER);

        // 预置 50 元工资入账
        walletService.credit(worker.getId(), 5_000L, WalletService.BIZ_WAGE, null, "预置工资");
        assertEquals(5_000L, walletService.getWallet(worker.getId()).getBalance());

        // 全额申请提现 -> 余额为 0
        WithDraw draw = walletService.applyWithdraw(worker.getId(), 5_000L, "mock-openid", "");
        assertEquals(0L, walletService.getWallet(worker.getId()).getBalance());

        // 模拟打款失败 -> 自动退回余额 + 退回流水 + 失败消息
        WithDraw failed = walletService.mockPayoutFail(draw.getId(), "模拟渠道失败");
        assertEquals(WithDrawStatus.FAILED, failed.getStatus());
        assertEquals(5_000L, walletService.getWallet(worker.getId()).getBalance());
        assertTrue(hasMessage(worker.getId(), MessageType.WITHDRAW_FAIL));

        List<WalletFlow> flows = walletService.listFlows(worker.getId());
        // WAGE(预置入账) + WITHDRAW(提现) + WITHDRAW_REFUND(退回)
        assertEquals(3, flows.size());
        assertTrue(flows.stream().anyMatch(f -> WalletService.BIZ_WITHDRAW.equals(f.getBizType())));
        assertTrue(flows.stream().anyMatch(f -> WalletService.BIZ_WITHDRAW_REFUND.equals(f.getBizType())));
    }
}

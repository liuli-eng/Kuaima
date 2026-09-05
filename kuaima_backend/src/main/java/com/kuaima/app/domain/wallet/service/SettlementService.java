package com.kuaima.app.domain.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.constant.MessageType;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.entity.Wallet;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 结算服务：老板对"已完成"的报名记录发起结算 -> 系统按 订单工资 × 工作天数 自动算工资(分)，
 * 并叠加平台服务费 -> 生成"待支付"结算单 -> 老板模拟支付成功后工资入零工钱包。
 * 服务费默认 0（费率规则待定，见 suma.settle.service-fee-rate）。
 */
@Service
public class SettlementService {

    private final SettlementRespository settlementRepository;
    private final BossOrderRespository orderRepository;
    private final BaseOrderItemRespository itemRepository;
    private final WalletService walletService;
    private final MessageService messageService;

    /** 平台服务费率(% of wage)，规则待定，默认 0 */
    @Value("${kuaima.settle.service-fee-rate:0}")
    private int serviceFeeRate;

    public SettlementService(SettlementRespository settlementRepository,
                             BossOrderRespository orderRepository,
                             BaseOrderItemRespository itemRepository,
                             WalletService walletService,
                             MessageService messageService) {
        this.settlementRepository = settlementRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.walletService = walletService;
        this.messageService = messageService;
    }

    /**
     * 创建结算单：报名记录必须已完成；同一报名记录不允许重复结算。
     *
     * @param workDays 实际工作天数，为空时由 到岗日~完成日 自动推导（最小 1 天）
     */
    @Transactional
    public Settlement createSettlement(Long itemId, Integer workDays) {
        BaseOrderItem item = getItemOrThrow(itemId);
        if (!BossStatus.ITEM_FINISHED.equals(item.getStatus())) {
            throw new IllegalStateException("仅已完成的报名记录可以发起结算");
        }
        if (settlementRepository.existsByItemIdAndStatusIn(itemId,
                List.of(SettlementStatus.PENDING, SettlementStatus.PAID))) {
            throw new IllegalStateException("该报名记录已存在待支付/已支付的结算单，请勿重复结算");
        }
        BossOrder order = orderRepository.findById(item.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("订单不存在: " + item.getOrderId()));
        if (order.getSalary() == null || order.getSalary() <= 0) {
            throw new IllegalStateException("订单工资未设置，无法结算");
        }
        // 实际工作天数：优先取传入值，否则按 到岗日~完成日 推导
        int days = resolveWorkDays(item, workDays);
        long wage = order.getSalary() * (long) days * 100L; // 元 -> 分
        long serviceFee = calculateServiceFee(wage);
        long total = wage + serviceFee;

        Settlement s = new Settlement();
        s.setItemId(itemId);
        s.setOrderId(item.getOrderId());
        s.setWorkerId(item.getUserId());
        s.setWorkDays(days);
        s.setWage(wage);
        s.setServiceFee(serviceFee);
        s.setTotalAmount(total);
        s.setStatus(SettlementStatus.PENDING);
        return settlementRepository.save(s);
    }

    /**
     * 模拟支付：待支付 -> 已支付；工资入零工钱包并记流水；服务费归平台（记录在结算单上）。
     */
    @Transactional
    public Settlement mockPay(Long settlementId) {
        Settlement s = getSettlementOrThrow(settlementId);
        if (!SettlementStatus.PENDING.equals(s.getStatus())) {
            throw new IllegalStateException("仅待支付的结算单可以支付");
        }
        s.setStatus(SettlementStatus.PAID);
        s.setPayNo("MOCK" + System.currentTimeMillis());
        s.setPayTime(LocalDateTime.now());
        settlementRepository.save(s);

        // 工资入零工钱包
        walletService.credit(s.getWorkerId(), s.getWage(), WalletService.BIZ_WAGE,
                s.getId(), "工资结算 orderId=" + s.getOrderId() + " 天数=" + s.getWorkDays());
        // 结算到账：通知零工
        messageService.sendToUser(s.getWorkerId(), MessageType.SETTLE_PAID, "工资已到账",
                "您的工资 " + fenToYuan(s.getWage()) + " 元已到账，可在钱包中查看或提现。",
                BizType.SETTLE, s.getId());
        return s;
    }

    /** 按订单查结算单 */
    public List<Settlement> listByOrder(Long orderId) {
        return settlementRepository.findByOrderIdOrderByIdDesc(orderId);
    }

    /** 按零工查结算单 */
    public List<Settlement> listByWorker(Long userId) {
        return settlementRepository.findByWorkerIdOrderByIdDesc(userId);
    }

    /** 零工钱包查询（复用钱包服务） */
    public Wallet getWorkerWallet(Long userId) {
        return walletService.getOrCreateWallet(userId);
    }

    /** 结算单详情 */
    public Settlement getSettlementDetail(Long id) {
        return getSettlementOrThrow(id);
    }

    // ==================== 内部方法 ====================

    private int resolveWorkDays(BaseOrderItem item, Integer workDays) {
        if (workDays != null && workDays > 0) {
            return workDays;
        }
        Date work = item.getWorkDate();
        Date finish = item.getFinishDate();
        if (work != null && finish != null && !finish.before(work)) {
            long span = ChronoUnit.DAYS.between(work.toLocalDate(), finish.toLocalDate()) + 1;
            return Math.max(1, (int) span);
        }
        return 1;
    }

    private long calculateServiceFee(long wage) {
        if (serviceFeeRate <= 0) {
            return 0L;
        }
        return BigDecimal.valueOf(wage)
                .multiply(BigDecimal.valueOf(serviceFeeRate))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValue();
    }

    private Settlement getSettlementOrThrow(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("结算单不存在: " + id));
    }

    /** 分 -> 元（去除末尾多余的 0），用于到账文案展示 */
    private String fenToYuan(long fen) {
        return BigDecimal.valueOf(fen)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private BaseOrderItem getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("报名记录不存在: " + id));
    }
}

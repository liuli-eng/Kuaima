package com.suma.app.domain.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suma.app.domain.message.constant.BizType;
import com.suma.app.domain.message.constant.MessageType;
import com.suma.app.domain.message.service.MessageService;
import com.suma.app.domain.wallet.constant.WithDrawStatus;
import com.suma.app.domain.wallet.entity.Wallet;
import com.suma.app.domain.wallet.entity.WalletFlow;
import com.suma.app.domain.wallet.entity.WithDraw;
import com.suma.app.domain.wallet.repository.WalletFlowRespository;
import com.suma.app.domain.wallet.repository.WalletRespository;
import com.suma.app.domain.wallet.repository.WithDrawRespository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 钱包账务服务：余额出入账 + 流水记录 + 提现申请/模拟打款。
 * 金额一律以分(Long)存储；真实支付/打款渠道后续接入，当前用模拟成功占位。
 */
@Service
public class WalletService {

    public static final String DIR_INCOME = "income";
    public static final String DIR_OUTCOME = "outcome";
    public static final String BIZ_WAGE = "WAGE";
    public static final String BIZ_WITHDRAW = "WITHDRAW";
    public static final String BIZ_WITHDRAW_REFUND = "WITHDRAW_REFUND";
    /** 提现渠道占位：真实接入后替换为微信商家转账 */
    public static final String CHANNEL_MOCK = "mock";

    private final WalletRespository walletRepository;
    private final WalletFlowRespository flowRepository;
    private final WithDrawRespository withdrawRepository;
    private final MessageService messageService;

    public WalletService(WalletRespository walletRepository,
                         WalletFlowRespository flowRepository,
                         WithDrawRespository withdrawRepository,
                         MessageService messageService) {
        this.walletRepository = walletRepository;
        this.flowRepository = flowRepository;
        this.withdrawRepository = withdrawRepository;
        this.messageService = messageService;
    }

    /** 查询钱包，不存在则创建空钱包（余额 0） */
    @Transactional
    public Wallet getOrCreateWallet(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(0L);
            return walletRepository.save(wallet);
        });
    }

    /** 查询钱包（只读，不自动创建） */
    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("钱包不存在: " + userId));
    }

    /**
     * 钱包入账并记录流水。
     *
     * @param amount 入账金额(分)，必须大于 0
     */
    @Transactional
    public Wallet credit(Long userId, Long amount, String bizType, Long bizId, String remark) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("入账金额必须大于 0");
        }
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);
        saveFlow(userId, DIR_INCOME, bizType, amount, wallet.getBalance(), bizId, remark);
        return wallet;
    }

    /**
     * 申请提现：校验余额后立即扣减，生成"申请中"提现单（模拟待打款）。
     */
    @Transactional
    public WithDraw applyWithdraw(Long userId, Long amount, String account, String remark) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("提现金额必须大于 0");
        }
        Wallet wallet = getOrCreateWallet(userId);
        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("余额不足，当前可用余额(分): " + wallet.getBalance());
        }
        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        WithDraw draw = new WithDraw();
        draw.setUserId(userId);
        draw.setAmount(amount);
        draw.setStatus(WithDrawStatus.PENDING);
        draw.setChannel(CHANNEL_MOCK);
        draw.setAccount(account);
        draw.setRemark(remark);
        draw.setApplyTime(LocalDateTime.now());
        withdrawRepository.save(draw);

        saveFlow(userId, DIR_OUTCOME, BIZ_WITHDRAW, amount, wallet.getBalance(), draw.getId(), "提现申请");
        return draw;
    }

    /** 模拟打款成功：申请中 -> 已打款（真实渠道：微信商家转账到零工账户） */
    @Transactional
    public WithDraw mockPayout(Long withdrawId) {
        WithDraw draw = getWithdrawOrThrow(withdrawId);
        if (!WithDrawStatus.PENDING.equals(draw.getStatus())) {
            throw new IllegalStateException("仅申请中的提现单可以打款");
        }
        draw.setStatus(WithDrawStatus.SUCCESS);
        draw.setPayTime(LocalDateTime.now());
        return withdrawRepository.save(draw);
    }

    /** 模拟打款失败：申请中 -> 打款失败，并退回余额到钱包 */
    @Transactional
    public WithDraw mockPayoutFail(Long withdrawId, String reason) {
        WithDraw draw = getWithdrawOrThrow(withdrawId);
        if (!WithDrawStatus.PENDING.equals(draw.getStatus())) {
            throw new IllegalStateException("仅申请中的提现单可以标记失败");
        }
        draw.setStatus(WithDrawStatus.FAILED);
        draw.setRemark(reason);
        withdrawRepository.save(draw);

        Wallet wallet = getOrCreateWallet(draw.getUserId());
        wallet.setBalance(wallet.getBalance() + draw.getAmount());
        walletRepository.save(wallet);
        saveFlow(draw.getUserId(), DIR_INCOME, BIZ_WITHDRAW_REFUND, draw.getAmount(),
                wallet.getBalance(), draw.getId(), "提现失败退回");
        // 打款失败：通知零工"已退回"
        messageService.sendToUser(draw.getUserId(), MessageType.WITHDRAW_FAIL, "提现打款失败",
                "您申请的提现 " + fenToYuan(draw.getAmount()) + " 元打款失败"
                        + (reason == null || reason.isBlank() ? "" : "（" + reason + "）")
                        + "，金额已退回钱包。",
                BizType.WITHDRAW, draw.getId());
        return draw;
    }

    /** 用户提现单列表 */
    public List<WithDraw> listWithdraws(Long userId) {
        return withdrawRepository.findByUserIdOrderByIdDesc(userId);
    }

    /** 用户钱包流水 */
    public List<WalletFlow> listFlows(Long userId) {
        return flowRepository.findByUserIdOrderByIdDesc(userId);
    }

    // ==================== 内部方法 ====================

    private void saveFlow(Long userId, String direction, String bizType,
                          Long amount, Long balanceAfter, Long bizId, String remark) {
        WalletFlow flow = new WalletFlow();
        flow.setUserId(userId);
        flow.setDirection(direction);
        flow.setBizType(bizType);
        flow.setAmount(amount);
        flow.setBalanceAfter(balanceAfter);
        flow.setBizId(bizId);
        flow.setRemark(remark);
        flowRepository.save(flow);
    }

    private WithDraw getWithdrawOrThrow(Long id) {
        return withdrawRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("提现单不存在: " + id));
    }

    /** 分 -> 元（去除末尾多余的 0），用于打款失败文案展示 */
    private String fenToYuan(long fen) {
        return BigDecimal.valueOf(fen)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
}

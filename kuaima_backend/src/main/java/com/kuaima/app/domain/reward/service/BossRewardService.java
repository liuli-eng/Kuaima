package com.kuaima.app.domain.reward.service;

import com.kuaima.app.domain.reward.entity.*;
import com.kuaima.app.domain.reward.repository.*;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Service
public class BossRewardService {
    public static final BigDecimal MIN_WITHDRAW_AMOUNT = new BigDecimal("10.00");
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final RewardAccountRepository accounts;
    private final RewardFlowRepository flows;
    private final RewardWithdrawalRepository withdrawals;
    private final UserRepository users;
    private final RewardWithdrawSettingsService settingsService;
    private final WorkerRewardWithdrawalProcessor withdrawalProcessor;
    private final WechatMerchantTransferClient transferClient;

    public BossRewardService(RewardAccountRepository accounts, RewardFlowRepository flows,
            RewardWithdrawalRepository withdrawals, UserRepository users,
            RewardWithdrawSettingsService settingsService, WorkerRewardWithdrawalProcessor withdrawalProcessor,
            WechatMerchantTransferClient transferClient) {
        this.accounts = accounts; this.flows = flows; this.withdrawals = withdrawals; this.users = users;
        this.settingsService = settingsService; this.withdrawalProcessor = withdrawalProcessor; this.transferClient = transferClient;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> overview(Long bossId) {
        requireBossExists(bossId);
        BigDecimal balance = accounts.findByUserId(bossId).map(a -> value(a.getBalance())).orElse(BigDecimal.ZERO);
        BigDecimal income = value(flows.sumByUserIdAndType(bossId, "INCOME"));
        BigDecimal expense = value(flows.sumByUserIdAndType(bossId, "EXPENSE"));
        return Map.of("balance", balance, "totalIncome", income, "totalExpense", expense,
                "withdrawableAmount", balance);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> records(Long bossId, String type, Pageable pageable) {
        requireBossExists(bossId);
        String normalized = type == null ? "ALL" : type.trim().toUpperCase();
        if (!Set.of("ALL", "INCOME", "EXPENSE").contains(normalized))
            throw new IllegalArgumentException("type 只能是 ALL、INCOME 或 EXPENSE");
        Page<RewardFlow> page = "ALL".equals(normalized)
                ? flows.findByUserIdOrderByCreatedAtDescIdDesc(bossId, pageable)
                : flows.findByUserIdAndTypeOrderByCreatedAtDescIdDesc(bossId, normalized, pageable);
        return page.map(this::flowView);
    }

    public Map<String, Object> withdraw(Long bossId, BigDecimal amount, String idempotencyKey) {
        if (amount == null) throw new IllegalArgumentException("amount 不能为空");
        BigDecimal normalized = amount.setScale(2, RoundingMode.UNNECESSARY);
        long amountFen = normalized.movePointRight(2).longValueExact();
        if (amountFen <= 0) throw new IllegalArgumentException("amount 必须大于0");
        String key = normalizeKey(bossId, idempotencyKey);
        users.findById(bossId).filter(UserRole::isBossIdentity)
                .orElseThrow(() -> new EntityNotFoundException("老板账号不存在: " + bossId));
        RewardWithdrawal existing = withdrawalProcessor.existing(bossId, amountFen, "WECHAT", key).orElse(null);
        if (existing != null) return withdrawalView(existing);
        RewardWithdrawal submitted;
        try {
            submitted = withdrawalProcessor.submit(bossId, amountFen, "WECHAT", settingsService.settings(), key);
        } catch (DataIntegrityViolationException e) {
            RewardWithdrawal concurrent = withdrawalProcessor.existing(bossId, amountFen, "WECHAT", key).orElse(null);
            if (concurrent != null) return withdrawalView(concurrent);
            throw e;
        }
        String openid = users.findById(bossId).map(com.kuaima.app.domain.user.entity.User::getOpenid).orElse(null);
        WechatMerchantTransferClient.MerchantTransferResult result;
        try {
            result = transferClient.transfer(new WechatMerchantTransferClient.MerchantTransferRequest(
                    submitted.getMerchantBatchNo(), submitted.getMerchantDetailNo(), amountFen, openid, "快马日结奖励金提现"));
        } catch (RuntimeException e) {
            withdrawalProcessor.transferFailed(submitted.getId(), e.getMessage());
            throw new WorkerRewardWithdrawalFailedException("微信商家转账发起失败，奖励金已退回");
        }
        return withdrawalView(withdrawalProcessor.transferAccepted(submitted, result));
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> withdrawals(Long bossId, Pageable pageable) {
        requireBossExists(bossId);
        return withdrawals.findByUserIdOrderByAppliedAtDescIdDesc(bossId, pageable).map(this::withdrawalView);
    }

    private void requireBossExists(Long bossId) {
        users.findById(bossId).filter(UserRole::isBossIdentity)
                .orElseThrow(() -> new EntityNotFoundException("老板账号不存在: " + bossId));
    }
    private Map<String, Object> flowView(RewardFlow flow) {
        Map<String, Object> result = new LinkedHashMap<>(); result.put("id", flow.getId()); result.put("type", flow.getType());
        result.put("amount", flow.getAmount()); result.put("title", flow.getTitle()); result.put("remark", flow.getRemark());
        result.put("bizType", flow.getBizType()); result.put("bizId", flow.getBizId()); result.put("createdAt", flow.getCreatedAt());
        return result;
    }
    private Map<String, Object> withdrawalView(RewardWithdrawal withdrawal) {
        Map<String, Object> result = new LinkedHashMap<>(); result.put("id", withdrawal.getId());
        result.put("amount", withdrawal.getAmount()); result.put("status", withdrawal.getStatus());
        result.put("appliedAt", withdrawal.getAppliedAt()); result.put("paidAt", withdrawal.getPaidAt());
        result.put("failureReason", withdrawal.getFailureReason()); return result;
    }
    private String normalizeKey(Long bossId, String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Idempotency-Key 不能为空");
        String trimmed = key.trim();
        if (trimmed.length() > 100) throw new IllegalArgumentException("Idempotency-Key 长度不能超过100");
        return "boss-reward:" + bossId + ":" + trimmed;
    }
    private BigDecimal value(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}

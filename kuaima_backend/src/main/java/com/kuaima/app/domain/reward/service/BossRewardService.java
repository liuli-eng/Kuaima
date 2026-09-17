package com.kuaima.app.domain.reward.service;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.reward.entity.*;
import com.kuaima.app.domain.reward.repository.*;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.entity.*;
import com.kuaima.app.domain.wallet.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BossRewardService {
    public static final long MIN_WITHDRAW_AMOUNT = 1000L;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final RewardAccountRepository accounts;
    private final RewardFlowRepository flows;
    private final RewardWithdrawalRepository withdrawals;
    private final RewardLedgerService ledger;
    private final WalletRespository wallets;
    private final WalletFlowRespository walletFlows;
    private final UserRepository users;

    public BossRewardService(RewardAccountRepository accounts, RewardFlowRepository flows,
            RewardWithdrawalRepository withdrawals, RewardLedgerService ledger,
            WalletRespository wallets, WalletFlowRespository walletFlows, UserRepository users) {
        this.accounts = accounts; this.flows = flows; this.withdrawals = withdrawals; this.ledger = ledger;
        this.wallets = wallets; this.walletFlows = walletFlows; this.users = users;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> overview(Long bossId) {
        requireBossExists(bossId);
        long balance = accounts.findByUserId(bossId).map(a -> value(a.getBalance())).orElse(0L);
        long income = value(flows.sumByUserIdAndType(bossId, "INCOME"));
        long expense = value(flows.sumByUserIdAndType(bossId, "EXPENSE"));
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

    @Transactional
    public Map<String, Object> withdraw(Long bossId, Long amount, String idempotencyKey) {
        if (amount == null) throw new IllegalArgumentException("amount 不能为空");
        if (amount < MIN_WITHDRAW_AMOUNT) throw new IllegalArgumentException("最低提现金额为1000分");
        String key = normalizeKey(bossId, amount, idempotencyKey);
        Optional<RewardWithdrawal> previous = withdrawals.findByIdempotencyKey(key);
        if (previous.isPresent()) return withdrawalView(requireSameRequest(previous.get(), bossId, amount));
        users.findByIdForUpdate(bossId).filter(u -> UserRole.BOSS.equals(u.getRole()))
                .orElseThrow(() -> new EntityNotFoundException("老板账号不存在: " + bossId));
        previous = withdrawals.findByIdempotencyKey(key);
        if (previous.isPresent()) return withdrawalView(requireSameRequest(previous.get(), bossId, amount));
        RewardAccount rewardAccount = ledger.lockedAccount(bossId);
        if (value(rewardAccount.getBalance()) < amount) throw new IllegalArgumentException("奖励金可提现余额不足");
        Wallet wallet = wallets.findByUserIdForUpdate(bossId)
                .orElseThrow(() -> new IllegalStateException("钱包不存在，无法提现奖励金"));
        if (value(wallet.getBalance()) < amount) throw new IllegalArgumentException("钱包余额不足，无法提现奖励金");

        RewardWithdrawal withdrawal = new RewardWithdrawal(); withdrawal.setUserId(bossId); withdrawal.setAmount(amount);
        withdrawal.setStatus("PENDING"); withdrawal.setIdempotencyKey(key); withdrawal.setAppliedAt(LocalDateTime.now(ZONE));
        withdrawal = withdrawals.saveAndFlush(withdrawal);
        rewardAccount.setBalance(value(rewardAccount.getBalance()) - amount); accounts.save(rewardAccount);
        wallet.setBalance(value(wallet.getBalance()) - amount); wallets.save(wallet);
        ledger.saveFlow(bossId, "EXPENSE", amount, rewardAccount.getBalance(), "奖励金提现", "奖励金提现申请",
                "WITHDRAW", withdrawal.getId(), "REWARD_WITHDRAW:" + withdrawal.getId());
        WalletFlow walletFlow = new WalletFlow(); walletFlow.setUserId(bossId); walletFlow.setDirection("outcome");
        walletFlow.setBizType("REWARD_WITHDRAW"); walletFlow.setAmount(amount); walletFlow.setBalanceAfter(wallet.getBalance());
        walletFlow.setBizId(withdrawal.getId()); walletFlow.setRemark("奖励金提现申请"); walletFlows.save(walletFlow);
        return withdrawalView(withdrawal);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> withdrawals(Long bossId, Pageable pageable) {
        requireBossExists(bossId);
        return withdrawals.findByUserIdOrderByAppliedAtDescIdDesc(bossId, pageable).map(this::withdrawalView);
    }

    private void requireBossExists(Long bossId) {
        users.findById(bossId).filter(u -> UserRole.BOSS.equals(u.getRole()))
                .orElseThrow(() -> new EntityNotFoundException("老板账号不存在: " + bossId));
    }
    private RewardWithdrawal requireOwner(RewardWithdrawal withdrawal, Long bossId) {
        if (!Objects.equals(withdrawal.getUserId(), bossId)) throw new ForbiddenBusinessException("无权访问该提现请求");
        return withdrawal;
    }
    private RewardWithdrawal requireSameRequest(RewardWithdrawal withdrawal, Long bossId, Long amount) {
        requireOwner(withdrawal, bossId);
        if (!Objects.equals(withdrawal.getAmount(), amount))
            throw new IllegalArgumentException("同一Idempotency-Key不能用于不同提现金额");
        return withdrawal;
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
    private String normalizeKey(Long bossId, Long amount, String key) {
        if (key != null && !key.isBlank()) {
            String trimmed = key.trim();
            if (trimmed.length() > 100) throw new IllegalArgumentException("Idempotency-Key 长度不能超过100");
            return bossId + ":" + trimmed;
        }
        return hash(bossId + "|" + amount + "|" + LocalDateTime.now(ZONE).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
    }
    private String hash(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("生成幂等键失败", e); }
    }
    private long value(Long value) { return value == null ? 0L : value; }
}

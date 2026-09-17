package com.kuaima.app.domain.reward.service;

import com.kuaima.app.domain.reward.entity.*;
import com.kuaima.app.domain.reward.repository.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardLedgerService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final RewardAccountRepository accounts;
    private final RewardFlowRepository flows;

    public RewardLedgerService(RewardAccountRepository accounts, RewardFlowRepository flows) {
        this.accounts = accounts; this.flows = flows;
    }

    @Transactional
    public void credit(Long userId, long amount, String bizType, Long bizId, String title, String remark, String sourceKey) {
        if (amount <= 0 || flows.findBySourceKey(sourceKey).isPresent()) return;
        RewardAccount account = lockedAccount(userId);
        account.setBalance(value(account.getBalance()) + amount);
        accounts.save(account);
        saveFlow(userId, "INCOME", amount, account.getBalance(), title, remark, bizType, bizId, sourceKey);
    }

    public RewardAccount lockedAccount(Long userId) {
        return accounts.findByUserIdForUpdate(userId).orElseGet(() -> {
            RewardAccount account = new RewardAccount(); account.setUserId(userId); account.setBalance(0L);
            return accounts.save(account);
        });
    }

    public void saveFlow(Long userId, String type, long amount, long balanceAfter, String title,
                         String remark, String bizType, Long bizId, String sourceKey) {
        RewardFlow flow = new RewardFlow(); flow.setUserId(userId); flow.setType(type); flow.setAmount(amount);
        flow.setBalanceAfter(balanceAfter); flow.setTitle(title); flow.setRemark(remark); flow.setBizType(bizType);
        flow.setBizId(bizId); flow.setSourceKey(sourceKey); flow.setCreatedAt(LocalDateTime.now(ZONE)); flows.save(flow);
    }

    private long value(Long value) { return value == null ? 0L : value; }
}

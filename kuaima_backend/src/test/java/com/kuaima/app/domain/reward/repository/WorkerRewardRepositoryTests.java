package com.kuaima.app.domain.reward.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;

import com.kuaima.app.domain.reward.entity.RewardAccount;
import com.kuaima.app.domain.reward.entity.RewardEarningRule;
import com.kuaima.app.domain.reward.entity.RewardFlow;
import com.kuaima.app.domain.reward.entity.RewardWithdrawal;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:worker_reward;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WorkerRewardRepositoryTests {
    @Autowired
    private RewardAccountRepository accounts;
    @Autowired
    private RewardFlowRepository flows;
    @Autowired
    private RewardWithdrawalRepository withdrawals;
    @Autowired
    private RewardEarningRuleRepository rules;

    @Test
    void rewardAccountHasOneRowPerUserAndOptimisticVersion() {
        RewardAccount account = new RewardAccount();
        account.setUserId(30L);
        account.setBalance(new BigDecimal("20.00"));
        account.setFrozenAmount(new BigDecimal("5.00"));
        account = accounts.saveAndFlush(account);

        account.setBalance(new BigDecimal("15.00"));
        account = accounts.saveAndFlush(account);

        assertEquals(1L, account.getVersion());

        RewardAccount duplicate = new RewardAccount();
        duplicate.setUserId(30L);
        assertThrows(DataIntegrityViolationException.class, () -> accounts.saveAndFlush(duplicate));
    }

    @Test
    void rewardFlowQueriesCurrentUserTypeAndCreatedOrder() {
        flows.save(flow(30L, "INCOME", "INVITE_FIRST_ORDER", new BigDecimal("8.00"), 2));
        flows.save(flow(30L, "EXPENSE", "WECHAT_WITHDRAW", new BigDecimal("15.00"), 3));
        flows.save(flow(31L, "INCOME", "INVITE_FIRST_ORDER", new BigDecimal("2.00"), 4));
        flows.flush();

        var all = flows.findByUserIdOrderByCreatedAtDescIdDesc(30L, PageRequest.of(0, 20));
        var income = flows.findByUserIdAndTypeOrderByCreatedAtDescIdDesc(30L, "INCOME", PageRequest.of(0, 20));
        var withdraw = flows.findByUserIdAndTypeOrderByCreatedAtDescIdDesc(30L, "EXPENSE", PageRequest.of(0, 20));

        assertEquals(2, all.getTotalElements());
        assertEquals(1, income.getTotalElements());
        assertEquals(1, withdraw.getTotalElements());
        assertEquals("WECHAT_WITHDRAW", withdraw.getContent().get(0).getBizType());
    }

    @Test
    void withdrawalIdempotencyKeyIsUniqueAndCanBeLockedByBusinessKey() {
        RewardWithdrawal withdrawal = new RewardWithdrawal();
        withdrawal.setUserId(30L);
        withdrawal.setAmount(new BigDecimal("10.00"));
        withdrawal.setChannel("WECHAT");
        withdrawal.setStatus("PENDING");
        withdrawal.setIdempotencyKey("worker-reward:30:request-1");
        withdrawal.setAppliedAt(LocalDateTime.now());
        withdrawals.saveAndFlush(withdrawal);

        assertEquals(withdrawal.getId(), withdrawals.findByIdempotencyKey("worker-reward:30:request-1").orElseThrow().getId());

        RewardWithdrawal duplicate = new RewardWithdrawal();
        duplicate.setUserId(30L);
        duplicate.setAmount(new BigDecimal("10.00"));
        duplicate.setChannel("WECHAT");
        duplicate.setStatus("PENDING");
        duplicate.setIdempotencyKey("worker-reward:30:request-1");
        duplicate.setAppliedAt(LocalDateTime.now());
        assertThrows(DataIntegrityViolationException.class, () -> withdrawals.saveAndFlush(duplicate));
    }

    @Test
    void earningRulesAreSelectedByEnabledAndSort() {
        rules.save(rule("ACTIVITY", 30));
        rules.save(rule("INVITE_FIRST_ORDER", 10));
        rules.flush();

        var enabled = rules.findByEnabledTrueOrderBySortAscIdAsc();

        assertEquals(2, enabled.size());
        assertEquals("INVITE_FIRST_ORDER", enabled.get(0).getCode());
    }

    private RewardFlow flow(Long userId, String type, String bizType, BigDecimal amount, int minute) {
        RewardFlow flow = new RewardFlow();
        flow.setUserId(userId);
        flow.setType(type);
        flow.setAmount(amount);
        flow.setBalanceAfter(amount);
        flow.setTitle(type.equals("INCOME") ? "奖励" : "提现");
        flow.setBizType(bizType);
        flow.setSourceKey("TEST:" + userId + ":" + bizType + ":" + minute);
        flow.setCreatedAt(LocalDateTime.of(2026, 9, 21, 18, minute));
        return flow;
    }

    private RewardEarningRule rule(String code, int sort) {
        RewardEarningRule rule = new RewardEarningRule();
        rule.setCode(code);
        rule.setName(code);
        rule.setActionType("NONE");
        rule.setEnabled(true);
        rule.setSort(sort);
        return rule;
    }
}

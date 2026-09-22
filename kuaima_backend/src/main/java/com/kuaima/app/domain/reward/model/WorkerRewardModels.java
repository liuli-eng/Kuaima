package com.kuaima.app.domain.reward.model;

public final class WorkerRewardModels {
    private WorkerRewardModels() {
    }

    public record WithdrawalRequest(Long amount, String channel) {
    }

    /** 老板端奖励金充值金额，单位为元。 */
    public record RechargeRequest(java.math.BigDecimal amount, String payMethod) {
    }
}

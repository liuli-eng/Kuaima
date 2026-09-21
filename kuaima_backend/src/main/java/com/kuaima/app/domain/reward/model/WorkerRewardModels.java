package com.kuaima.app.domain.reward.model;

public final class WorkerRewardModels {
    private WorkerRewardModels() {
    }

    public record WithdrawalRequest(Long amount, String channel) {
    }
}

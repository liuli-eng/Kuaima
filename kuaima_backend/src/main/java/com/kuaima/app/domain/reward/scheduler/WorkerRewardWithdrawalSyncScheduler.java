package com.kuaima.app.domain.reward.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kuaima.app.domain.reward.service.WorkerRewardWithdrawalSyncService;

@Component
public class WorkerRewardWithdrawalSyncScheduler {
    private final WorkerRewardWithdrawalSyncService service;

    public WorkerRewardWithdrawalSyncScheduler(WorkerRewardWithdrawalSyncService service) {
        this.service = service;
    }

    @Scheduled(
            initialDelayString = "${jobs.reward-withdraw.sync-initial-delay:60000}",
            fixedDelayString = "${jobs.reward-withdraw.sync-fixed-delay:60000}")
    public void sync() {
        service.syncPending();
    }
}

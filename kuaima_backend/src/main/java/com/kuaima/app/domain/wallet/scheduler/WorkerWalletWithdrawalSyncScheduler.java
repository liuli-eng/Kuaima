package com.kuaima.app.domain.wallet.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kuaima.app.domain.wallet.service.WorkerWalletWithdrawalSyncService;

@Component
public class WorkerWalletWithdrawalSyncScheduler {
    private final WorkerWalletWithdrawalSyncService service;
    public WorkerWalletWithdrawalSyncScheduler(WorkerWalletWithdrawalSyncService service) { this.service = service; }

    @Scheduled(initialDelayString = "${jobs.wallet-withdraw.sync-initial-delay:60000}",
            fixedDelayString = "${jobs.wallet-withdraw.sync-fixed-delay:60000}")
    public void sync() { service.syncPending(); }
}

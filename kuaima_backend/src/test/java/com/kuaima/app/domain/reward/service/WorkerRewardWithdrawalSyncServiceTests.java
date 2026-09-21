package com.kuaima.app.domain.reward.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.reward.entity.RewardWithdrawal;
import com.kuaima.app.domain.reward.repository.RewardWithdrawalRepository;
import com.kuaima.app.domain.reward.service.WechatMerchantTransferClient.MerchantTransferStatus;

class WorkerRewardWithdrawalSyncServiceTests {
    private RewardWithdrawalRepository withdrawals;
    private WorkerRewardWithdrawalProcessor processor;
    private WechatMerchantTransferClient client;
    private WorkerRewardWithdrawalSyncService service;
    private RewardWithdrawal pending;

    @BeforeEach
    void setUp() {
        withdrawals = mock(RewardWithdrawalRepository.class);
        processor = mock(WorkerRewardWithdrawalProcessor.class);
        client = mock(WechatMerchantTransferClient.class);
        service = new WorkerRewardWithdrawalSyncService(withdrawals, processor, client);
        pending = new RewardWithdrawal();
        pending.setId(90001L);
        pending.setMerchantBatchNo("RWB2026092118300012345678");
        when(withdrawals.findTop20ByStatusAndAppliedAtBeforeOrderByAppliedAtAscIdAsc(
                eq("PENDING"), any(LocalDateTime.class))).thenReturn(List.of(pending));
    }

    @Test
    void finishedSuccessUpdatesWithdrawalAndFlowThroughProcessor() {
        MerchantTransferStatus status = new MerchantTransferStatus(
                "FINISHED", "SUCCESS", "detail-1", "{\"batch_status\":\"FINISHED\"}");
        when(client.query(pending.getMerchantBatchNo())).thenReturn(status);

        int changed = service.syncPending();

        assertEquals(1, changed);
        verify(processor).transferSucceeded(90001L, "detail-1", status.rawResponse());
        verify(processor, never()).transferFailed(any(), anyString(), anyString(), anyString());
    }

    @Test
    void closedOrFailedDetailRefundsReward() {
        MerchantTransferStatus status = new MerchantTransferStatus(
                "CLOSED", null, null, "{\"batch_status\":\"CLOSED\"}");
        when(client.query(pending.getMerchantBatchNo())).thenReturn(status);

        int changed = service.syncPending();

        assertEquals(1, changed);
        verify(processor).transferFailed(90001L, "微信商家转账失败", null, status.rawResponse());
        verify(processor, never()).transferSucceeded(any(), anyString(), anyString());
    }

    @Test
    void processingBatchRemainsPending() {
        when(client.query(pending.getMerchantBatchNo())).thenReturn(
                new MerchantTransferStatus("PROCESSING", "PROCESSING", "detail-1", "{}"));

        assertEquals(0, service.syncPending());

        verify(processor, never()).transferSucceeded(any(), anyString(), anyString());
        verify(processor, never()).transferFailed(any(), anyString(), anyString(), anyString());
    }

    @Test
    void queryFailureDoesNotMistakenlyRefund() {
        when(client.query(pending.getMerchantBatchNo())).thenThrow(new IllegalStateException("查询失败"));

        assertEquals(0, service.syncPending());

        verify(processor, never()).transferFailed(any(), anyString(), anyString(), anyString());
    }
}

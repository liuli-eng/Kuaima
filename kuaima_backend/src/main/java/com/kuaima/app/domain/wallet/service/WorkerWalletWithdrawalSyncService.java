package com.kuaima.app.domain.wallet.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.kuaima.app.domain.reward.service.WechatMerchantTransferClient;
import com.kuaima.app.domain.wallet.constant.WithDrawStatus;
import com.kuaima.app.domain.wallet.repository.WithDrawRespository;

@Service
public class WorkerWalletWithdrawalSyncService {
    private final WithDrawRespository withdrawals;
    private final WalletService wallets;
    private final WechatMerchantTransferClient transfers;

    public WorkerWalletWithdrawalSyncService(WithDrawRespository withdrawals, WalletService wallets,
            WechatMerchantTransferClient transfers) {
        this.withdrawals = withdrawals; this.wallets = wallets; this.transfers = transfers;
    }

    public int syncPending() {
        int changed = 0;
        var pending = withdrawals.findTop20ByStatusAndApplyTimeBeforeAndMerchantBatchNoIsNotNullOrderByApplyTimeAsc(
                WithDrawStatus.PENDING, LocalDateTime.now().minusMinutes(1));
        for (var draw : pending) {
            WechatMerchantTransferClient.MerchantTransferStatus status;
            try { status = transfers.query(draw.getMerchantBatchNo()); }
            catch (RuntimeException e) { continue; }
            if ("FINISHED".equals(status.batchStatus()) && "SUCCESS".equals(status.detailStatus())) {
                wallets.succeedWechatWithdraw(draw.getId(), status.detailId(), status.rawResponse()); changed++;
            } else if ("CLOSED".equals(status.batchStatus()) || "FAIL".equals(status.detailStatus())) {
                wallets.failWechatWithdraw(draw.getId(), "微信提现失败", status.detailId(), status.rawResponse()); changed++;
            }
        }
        return changed;
    }
}

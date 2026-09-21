package com.kuaima.app.domain.reward.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.reward.entity.RewardWithdrawal;
import com.kuaima.app.domain.reward.repository.RewardWithdrawalRepository;
import com.kuaima.app.domain.reward.service.WechatMerchantTransferClient.MerchantTransferStatus;

/** 同步微信商家转账终态；查询失败不视为转账失败，避免查询异常导致误退款。 */
@Service
public class WorkerRewardWithdrawalSyncService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Logger log = LoggerFactory.getLogger(WorkerRewardWithdrawalSyncService.class);

    private final RewardWithdrawalRepository withdrawals;
    private final WorkerRewardWithdrawalProcessor processor;
    private final WechatMerchantTransferClient transferClient;

    public WorkerRewardWithdrawalSyncService(RewardWithdrawalRepository withdrawals,
            WorkerRewardWithdrawalProcessor processor, WechatMerchantTransferClient transferClient) {
        this.withdrawals = withdrawals;
        this.processor = processor;
        this.transferClient = transferClient;
    }

    public int syncPending() {
        LocalDateTime due = LocalDateTime.now(ZONE).minusMinutes(1);
        List<RewardWithdrawal> pending = withdrawals
                .findTop20ByStatusAndAppliedAtBeforeOrderByAppliedAtAscIdAsc("PENDING", due);
        int changed = 0;
        for (RewardWithdrawal withdrawal : pending) {
            if (!StringUtils.hasText(withdrawal.getMerchantBatchNo())) continue;
            MerchantTransferStatus status;
            try {
                status = transferClient.query(withdrawal.getMerchantBatchNo());
            } catch (RuntimeException e) {
                // 查询失败不代表转账失败；保留 PENDING，等待下一轮查询。
                log.warn("奖励金提现单 {} 微信状态查询失败: {}", withdrawal.getId(), e.getMessage());
                continue;
            }
            boolean terminalSuccess = "FINISHED".equals(status.batchStatus()) && "SUCCESS".equals(status.detailStatus());
            boolean terminalFailure = "CLOSED".equals(status.batchStatus()) || "FAIL".equals(status.detailStatus());
            if (terminalSuccess) {
                processor.transferSucceeded(withdrawal.getId(), status.detailId(), status.rawResponse());
                changed++;
            } else if (terminalFailure) {
                processor.transferFailed(withdrawal.getId(), "微信商家转账失败",
                        status.detailId(), status.rawResponse());
                changed++;
            }
        }
        return changed;
    }
}

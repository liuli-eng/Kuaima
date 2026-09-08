package com.kuaima.app.domain.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 老板待结算页面的订单聚合视图。金额同时提供元和分，避免前端重复换算。 */
public final class PendingSettlementModels {

    private PendingSettlementModels() {
    }

    public record PendingSettlementOrder(
            Long id,
            Long orderId,
            LocalDate date,
            String job,
            BigDecimal amount,
            Long amountFen,
            Integer workerCount,
            List<String> workers,
            String status,
            String statusText,
            List<PendingSettlementItem> items) {
    }

    public record PendingSettlementItem(
            Long itemId,
            Long workerId,
            String workerName,
            Long settlementId,
            String settlementStatus,
            BigDecimal amount,
            Long amountFen,
            Integer workDays) {
    }
}

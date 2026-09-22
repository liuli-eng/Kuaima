package com.kuaima.app.domain.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class SettlementPaymentModels {
    private SettlementPaymentModels() {}

    public record WechatPayRequest(List<Long> settlementIds) {}

    public record WechatPayView(String paymentNo, List<Long> settlementIds, BigDecimal amount,
                                String status, Map<String, Object> payParams,
                                LocalDateTime createdAt, LocalDateTime paidAt) {}
}

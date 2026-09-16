package com.kuaima.app.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PointPurchaseDtos {
    private PointPurchaseDtos() {}
    public record CreateRequest(Long bossId, Long points, String payMethod, String deal, String remark, String idempotencyKey) {}
    public record OrderResponse(Long id, String orderNo, Long bossId, String bossName, String companyName,
            Long points, BigDecimal amount, BigDecimal unitPrice, String payMethod, String status,
            LocalDateTime purchaseTime, String remark, Long operatorId, String operatorName) {}
}

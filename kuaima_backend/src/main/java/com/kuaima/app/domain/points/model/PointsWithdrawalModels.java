package com.kuaima.app.domain.points.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public final class PointsWithdrawalModels {
    private PointsWithdrawalModels() {}
    public record ApplyRequest(Integer points, String channel) {}
    public record RejectRequest(String reason) {}
    public record PageView<T>(List<T> content, int page, int size, long total) {}
    public record WithdrawalView(Long id, String withdrawNo, Integer points, BigDecimal amount,
                                 String channel, String status, BigDecimal fee, String failureReason,
                                 LocalDateTime appliedAt, OffsetDateTime expectedArrivalAt,
                                 LocalDateTime processingAt, LocalDateTime paidAt) {}
}

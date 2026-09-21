package com.kuaima.app.domain.points.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "points_withdrawal", uniqueConstraints = {
        @UniqueConstraint(name = "uk_points_withdrawal_no", columnNames = "withdraw_no"),
        @UniqueConstraint(name = "uk_points_withdrawal_idempotency", columnNames = "idempotency_key")
})
@Getter
@Setter
public class PointsWithdrawal extends BaseEntity {
    @Column(name = "withdraw_no", nullable = false, length = 64)
    private String withdrawNo;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, length = 20)
    private String role = "USER";
    @Column(nullable = false)
    private Integer points;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO.setScale(2);
    @Column(nullable = false, length = 20)
    private String channel;
    @Column(name = "channel_account", length = 128)
    private String channelAccount;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;
    private LocalDateTime appliedAt;
    private LocalDateTime processingAt;
    private LocalDateTime paidAt;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    @Column(length = 500)
    private String failureReason;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

package com.kuaima.app.domain.reward.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reward_flow", indexes = @Index(name = "idx_reward_flow_user_created", columnList = "user_id,created_at"),
        uniqueConstraints = @UniqueConstraint(name = "uk_reward_flow_source", columnNames = "source_key"))
@Getter @Setter
public class RewardFlow {
    public void setAmount(Long value) { this.amount = value == null ? null : BigDecimal.valueOf(value); }
    public void setAmount(long value) { this.amount = BigDecimal.valueOf(value); }
    public void setAmount(BigDecimal value) { this.amount = value; }
    public void setBalanceAfter(Long value) { this.balanceAfter = value == null ? null : BigDecimal.valueOf(value); }
    public void setBalanceAfter(long value) { this.balanceAfter = BigDecimal.valueOf(value); }
    public void setBalanceAfter(BigDecimal value) { this.balanceAfter = value; }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 20) private String role = "USER";
    @Column(nullable = false, length = 10) private String type;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal amount;
    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2) private BigDecimal balanceAfter;
    @Column(length = 100) private String title;
    @Column(length = 500) private String remark;
    @Column(name = "biz_type", length = 50) private String bizType;
    @Column(name = "biz_id") private Long bizId;
    @Column(name = "source_key", nullable = false, length = 128) private String sourceKey;
    @Column(name = "idempotency_key", length = 128) private String idempotencyKey;
    @Column(length = 20) private String status;
    @Column(length = 500) private String description;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}

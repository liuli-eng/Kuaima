package com.kuaima.app.domain.reward.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reward_account", uniqueConstraints = @UniqueConstraint(name = "uk_reward_account_user", columnNames = "user_id"))
@Getter @Setter
public class RewardAccount {
    public void setBalance(Long value) { this.balance = value == null ? null : BigDecimal.valueOf(value); }
    public void setBalance(long value) { this.balance = BigDecimal.valueOf(value); }
    public void setBalance(BigDecimal value) { this.balance = value; }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /** 冻结金额，单位与 balance 一致（元）。 */
    @Column(name = "frozen_amount", precision = 18, scale = 2)
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    /** 乐观锁版本；余额变更同时配合悲观行锁使用。 */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    void prePersist() {
        createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }

    public void setFrozenAmount(Long value) {
        this.frozenAmount = value == null ? null : BigDecimal.valueOf(value);
    }

    public void setFrozenAmount(long value) {
        this.frozenAmount = BigDecimal.valueOf(value);
    }

    public void setFrozenAmount(BigDecimal value) {
        this.frozenAmount = value;
    }
}

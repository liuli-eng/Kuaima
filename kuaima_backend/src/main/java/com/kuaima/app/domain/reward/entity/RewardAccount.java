package com.kuaima.app.domain.reward.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
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
}

package com.kuaima.app.domain.reward.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reward_account", uniqueConstraints = @UniqueConstraint(name = "uk_reward_account_user", columnNames = "user_id"))
@Getter @Setter
public class RewardAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long balance = 0L;
}

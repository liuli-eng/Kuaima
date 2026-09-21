package com.kuaima.app.domain.reward.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** 零工端奖励金获取规则。rewardAmount 统一保存“分”。 */
@Entity
@Table(name = "reward_earning_rule", uniqueConstraints = @UniqueConstraint(name = "uk_reward_earning_rule_code", columnNames = "code"))
@Getter
@Setter
public class RewardEarningRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "reward_amount")
    private Long rewardAmount;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "action_path", length = 200)
    private String actionPath;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Integer sort = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        updatedAt = updatedAt == null ? createdAt : updatedAt;
    }
}

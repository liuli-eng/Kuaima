package com.kuaima.app.domain.reward.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reward_flow", indexes = @Index(name = "idx_reward_flow_user_created", columnList = "user_id,created_at"),
        uniqueConstraints = @UniqueConstraint(name = "uk_reward_flow_source", columnNames = "source_key"))
@Getter @Setter
public class RewardFlow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 10) private String type;
    @Column(nullable = false) private Long amount;
    @Column(name = "balance_after", nullable = false) private Long balanceAfter;
    @Column(length = 100) private String title;
    @Column(length = 500) private String remark;
    @Column(name = "biz_type", length = 50) private String bizType;
    @Column(name = "biz_id") private Long bizId;
    @Column(name = "source_key", nullable = false, length = 128) private String sourceKey;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}

package com.kuaima.app.domain.review.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "boss_review",
        uniqueConstraints = @UniqueConstraint(name = "uk_boss_review_item", columnNames = "item_id"),
        indexes = {
                @Index(name = "idx_boss_review_boss_created", columnList = "boss_id,created_at"),
                @Index(name = "idx_boss_review_worker_created", columnList = "worker_id,created_at")
        })
@Check(constraints = "attitude_score between 1 and 5 and settlement_score between 1 and 5 and environment_score between 1 and 5")
@Getter @Setter
public class BossReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "item_id", nullable = false) private Long itemId;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(name = "worker_id", nullable = false) private Long workerId;
    @Column(name = "boss_id", nullable = false) private Long bossId;
    @Column(name = "attitude_score", nullable = false) private Integer attitudeScore;
    @Column(name = "settlement_score", nullable = false) private Integer settlementScore;
    @Column(name = "environment_score", nullable = false) private Integer environmentScore;
    @Column(length = 200) private String content;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    @PrePersist void create() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void update() { updatedAt = LocalDateTime.now(); }
}

package com.kuaima.app.domain.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 老板对订单内单个零工的评价。 */
@Entity
@Table(name = "worker_review",
        uniqueConstraints = @UniqueConstraint(name = "uk_worker_review_item", columnNames = "item_id"),
        indexes = {
                @Index(name = "idx_worker_review_worker_created", columnList = "worker_id,created_at"),
                @Index(name = "idx_worker_review_order", columnList = "order_id")
        })
@Getter
@Setter
public class WorkerReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "item_id", nullable = false) private Long itemId;
    @Column(name = "order_id", nullable = false) private Long orderId;
    @Column(name = "worker_id", nullable = false) private Long workerId;
    @Column(name = "boss_id", nullable = false) private Long bossId;
    @Column(name = "overall_score", nullable = false) private Integer overallScore;
    @Column(name = "attitude_score", nullable = false) private Integer attitudeScore;
    @Column(name = "efficiency_score", nullable = false) private Integer efficiencyScore;
    @Column(name = "skill_score", nullable = false) private Integer skillScore;
    @Column(length = 200) private String content;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    @PrePersist void create() { createdAt = LocalDateTime.now(); }
}

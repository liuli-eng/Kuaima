package com.kuaima.app.domain.points.entity;
import java.time.LocalDateTime;import jakarta.persistence.*;import lombok.Getter;import lombok.Setter;
@Entity @Table(name="points_gift",uniqueConstraints=@UniqueConstraint(name="uk_points_gift_key",columnNames="idempotency_key")) @Getter @Setter
public class PointsGift {@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(name="idempotency_key",nullable=false,length=128)private String idempotencyKey;private Long bossId;private Long workerId;private Integer points;private LocalDateTime createTime;}

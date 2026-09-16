package com.kuaima.app.domain.points.entity;
import jakarta.persistence.*;
import lombok.Getter;import lombok.Setter;
@Entity @Table(name="points_package") @Getter @Setter
public class PointsPackage { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private Integer points; @Column(nullable=false) private Integer price; @Column(length=100) private String tag; private Boolean hot=false; private Boolean enabled=true; }

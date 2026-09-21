package com.kuaima.app.admin.entity;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="point_package", indexes={@Index(name="idx_point_package_points", columnList="points"), @Index(name="idx_point_package_price", columnList="price")})
@Getter @Setter
public class PointPackageSetting {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=20) private String name;
    @Column(length=100) private String sub;
    @Column(nullable=false) private Long points;
    /** 套餐售价，单位：人民币元，精确到分。 */
    @Column(nullable=false, precision=10, scale=2) private BigDecimal price;
    @Column(length=50) private String originalPrice;
    @Column(name="save_text",length=50) private String save;
    private Boolean rec=false;
    private Integer sort=0;
    private Boolean enabled=true;
    private Long operatorId;
    @Column(length=100) private String operatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist void create(){createdAt=updatedAt=LocalDateTime.now();}
    @PreUpdate void update(){updatedAt=LocalDateTime.now();}
}

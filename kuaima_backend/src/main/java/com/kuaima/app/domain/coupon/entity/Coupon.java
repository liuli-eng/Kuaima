package com.kuaima.app.domain.coupon.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 优惠券模板：平台发行的优惠券，用户可领取并消费使用。
 */
@Entity
@Table(name = "coupon", indexes = {
        @Index(name="idx_coupon_status_grant", columnList="status,grant_start"),
        @Index(name="idx_coupon_valid", columnList="valid_start,valid_end"),
        @Index(name="idx_coupon_target", columnList="target"),
        @Index(name="idx_coupon_type", columnList="type")
})
@Getter
@Setter
public class Coupon extends BaseEntity {

    @Column(length = 100, comment = "优惠券名称")
    private String title;

    @Column(length = 20, comment = "类型:满减(FULL)/折扣(DISCOUNT) 等")
    private String type;

    @Column(comment = "优惠金额(元)")
    private BigDecimal amount;

    @Column(comment = "使用门槛(元),满 minSpend 元可用")
    private BigDecimal minSpend;

    @Column(length = 20, comment = "状态:发行中(ISSUED)/已下架(OFF)")
    private String status;

    @Column(unique = true, length = 40) private String couponNo;
    @Column(length = 100) private String name;
    @Column(precision = 12, scale = 2) private BigDecimal threshold;
    @Column(precision = 5, scale = 2) private BigDecimal discount;
    @Column(precision = 12, scale = 2) private BigDecimal cap;
    @Column(length = 20) private String target;
    @Column(length = 20) private String scope;
    @Column(columnDefinition = "TEXT") private String assignUsers;
    private Integer total;
    private Integer limitPerUser;
    private Integer claimed;
    private Integer used;
    @Column(length = 20) private String grantMode;
    private LocalDateTime grantStart;
    @Column(length = 20) private String validMode;
    private LocalDateTime validStart;
    private LocalDateTime validEnd;
    private Integer validDays;
    @Column(length = 500) private String description;
    private Boolean deleted = false;
    private Boolean stopped = false;
}

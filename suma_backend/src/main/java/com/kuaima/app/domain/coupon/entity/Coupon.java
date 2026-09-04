package com.kuaima.app.domain.coupon.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 优惠券模板：平台发行的优惠券，用户可领取并消费使用。
 */
@Entity
@Table(name = "coupon")
@Getter
@Setter
public class Coupon extends BaseEntity {

    @Column(length = 100, comment = "优惠券名称")
    private String title;

    @Column(length = 20, comment = "类型:满减(FULL)/折扣(DISCOUNT) 等")
    private String type;

    @Column(comment = "优惠金额(元)")
    private Double amount;

    @Column(comment = "使用门槛(元),满 minSpend 元可用")
    private Double minSpend;

    @Column(comment = "有效天数(自领取起算)")
    private Integer validDays;

    @Column(length = 20, comment = "状态:发行中(ISSUED)/已下架(OFF)")
    private String status;
}

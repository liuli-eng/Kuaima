package com.kuaima.app.domain.coupon.entity;

import java.sql.Date;
import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户领取的优惠券：领取时按券面 validDays 计算到期时间，使用后置为已使用。
 */
@Entity
@Table(name = "user_coupon")
@Getter
@Setter
public class UserCoupon extends BaseEntity {

    @Column(comment = "用户ID")
    private Long userId;

    @Column(comment = "优惠券ID")
    private Long couponId;

    @Column(length = 20, comment = "状态:未使用(UNUSED)/已使用(USED)/已过期(EXPIRED)")
    private String status;

    @Column(comment = "到期时间")
    private Date expireAt;

    @Column(comment = "使用时间")
    private Timestamp usedAt;
}

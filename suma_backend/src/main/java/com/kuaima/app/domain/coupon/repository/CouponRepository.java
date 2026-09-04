package com.kuaima.app.domain.coupon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.coupon.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /** 按状态查优惠券 */
    List<Coupon> findByStatus(String status);
}

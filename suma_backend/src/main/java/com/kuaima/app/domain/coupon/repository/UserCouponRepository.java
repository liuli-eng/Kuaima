package com.kuaima.app.domain.coupon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.coupon.entity.UserCoupon;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    /** 按用户查优惠券 */
    List<UserCoupon> findByUserId(Long userId);

    /** 按用户+状态查优惠券 */
    List<UserCoupon> findByUserIdAndStatus(Long userId, String status);
}

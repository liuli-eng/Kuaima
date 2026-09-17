package com.kuaima.app.domain.coupon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.kuaima.app.domain.coupon.entity.UserCoupon;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    /** 按用户查优惠券 */
    List<UserCoupon> findByUserId(Long userId);

    /** 按用户+状态查优惠券 */
    List<UserCoupon> findByUserIdAndStatus(Long userId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select uc from UserCoupon uc where uc.id = :id")
    java.util.Optional<UserCoupon> findByIdForUpdate(@Param("id") Long id);

    /** 统计用户未使用且未过期的券包数量。过期时间为空表示长期有效。 */
    long countByUserIdAndStatusAndExpireAtIsNull(Long userId, String status);

    long countByUserIdAndStatusAndExpireAtGreaterThanEqual(Long userId, String status, java.sql.Date now);
    long countByCouponId(Long couponId);
    long countByCouponIdAndStatus(Long couponId, String status);
    boolean existsByCouponId(Long couponId);
    long countByCouponIdAndUserId(Long couponId, Long userId);
}

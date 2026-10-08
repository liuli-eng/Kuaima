package com.kuaima.app.domain.coupon.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.kuaima.app.domain.coupon.entity.UserCoupon;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    /** 老板优惠券列表一次 JOIN 用户、用户券和券模板，减少远程数据库往返。 */
    @Query(value = """
            select uc.id as id, uc.status as status, uc.expire_at as expireAt,
                   uc.used_at as usedAt, uc.use_order_id as useOrderId,
                   coalesce(c.title, c.name, '优惠券') as title, c.type as type,
                   c.amount as amount, c.min_spend as minSpend, c.discount as discount,
                   c.cap as cap, count(*) over() as totalCount
            from user_coupon uc
            left join coupon c on c.id = uc.coupon_id
            join sys_user u on u.id = uc.user_id
             and (upper(coalesce(u.enterprise_status, '')) = 'APPROVED'
                  or (upper(coalesce(u.cert_type, '')) = 'ENTERPRISE' and u.cert_status = '已通过'))
            where uc.user_id = :userId
              and (:status = 'ALL'
                   or (:status = 'AVAILABLE' and uc.status = 'UNUSED'
                       and (uc.expire_at is null or uc.expire_at >= :today))
                   or (:status = 'HISTORY' and (uc.status <> 'UNUSED'
                       or (uc.expire_at is not null and uc.expire_at < :today))))
            order by uc.id desc
            limit :limitValue offset :offsetValue
            """, nativeQuery = true)
    List<AdminCouponRecordRow> findAdminCouponRecords(@Param("userId") Long userId,
                                                       @Param("status") String status,
                                                       @Param("today") Date today,
                                                       @Param("limitValue") int limitValue,
                                                       @Param("offsetValue") int offsetValue);

    /** 按用户查优惠券 */
    List<UserCoupon> findByUserId(Long userId);

    List<UserCoupon> findByUseOrderIdIn(java.util.Collection<Long> orderIds);

    /** 按用户+状态查优惠券 */
    List<UserCoupon> findByUserIdAndStatus(Long userId, String status);

    /** 后台老板详情：可用优惠券包含未使用且未过期的券。 */
    @Query("""
            select uc from UserCoupon uc
            where uc.userId = :userId
              and uc.status = 'UNUSED'
              and (uc.expireAt is null or uc.expireAt >= :today)
            order by uc.id desc
            """)
    org.springframework.data.domain.Page<UserCoupon> findAvailableByUserId(
            @Param("userId") Long userId,
            @Param("today") Date today,
            org.springframework.data.domain.Pageable pageable);

    /** 后台老板详情：历史券包含已使用、已过期或状态不是未使用的券。 */
    @Query("""
            select uc from UserCoupon uc
            where uc.userId = :userId
              and (uc.status <> 'UNUSED' or (uc.expireAt is not null and uc.expireAt < :today))
            order by uc.id desc
            """)
    org.springframework.data.domain.Page<UserCoupon> findHistoryByUserId(
            @Param("userId") Long userId,
            @Param("today") Date today,
            org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<UserCoupon> findByUserIdOrderByIdDesc(Long userId,
            org.springframework.data.domain.Pageable pageable);

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

    @Query("select uc.userId from UserCoupon uc where uc.couponId = :couponId")
    List<Long> findUserIdsByCouponId(@Param("couponId") Long couponId);
}

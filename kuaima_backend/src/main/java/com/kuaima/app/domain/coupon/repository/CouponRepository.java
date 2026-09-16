package com.kuaima.app.domain.coupon.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.coupon.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /** 按状态查优惠券 */
    List<Coupon> findByStatus(String status);

    @Query("select c from Coupon c where coalesce(c.deleted,false)=false and (:status is null or c.status=:status) and (:type is null or c.type=:type) and (:target is null or c.target=:target) and (:keyword is null or lower(coalesce(c.name,c.title)) like lower(concat('%',:keyword,'%'))) order by c.id desc")
    Page<Coupon> search(@Param("status") String status, @Param("type") String type, @Param("target") String target, @Param("keyword") String keyword, Pageable pageable);

    @Query("select count(c) from Coupon c where coalesce(c.deleted,false)=false and c.status=:status") long countStatus(@Param("status") String status);
    @Query("select coalesce(sum(c.used),0) from Coupon c where coalesce(c.deleted,false)=false") long sumUsed();
    @Query("select coalesce(sum(c.claimed),0) from Coupon c where coalesce(c.deleted,false)=false") long sumClaimed();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where c.id=:id")
    Optional<Coupon> findByIdForUpdate(@Param("id") Long id);
}

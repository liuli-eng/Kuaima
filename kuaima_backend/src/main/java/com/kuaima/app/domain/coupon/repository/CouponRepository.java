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

    /**
     * 查询所有已经到发放时间、但仍未达到发放总量的优惠券。
     *
     * 不能限制 grantMode=timing：立即发放券创建时如果因临时异常没有写入
     * user_coupon，也必须由补发任务兜底；claimed 使用 coalesce 兼容历史空值。
     */
    @Query("select c.id from Coupon c where coalesce(c.deleted,false)=false and coalesce(c.stopped,false)=false and c.grantStart is not null and c.grantStart<=:now and coalesce(c.claimed,0) < coalesce(c.total,0)")
    List<Long> findDueAutoDistributionIds(@Param("now") java.time.LocalDateTime now);
}

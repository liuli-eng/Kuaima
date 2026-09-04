package com.kuaima.app.domain.boss.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.boss.entity.BaseOrderItem;

public interface BaseOrderItemRespository extends JpaRepository<BaseOrderItem, Long> {

    /** 查询某订单的所有报名记录 */
    List<BaseOrderItem> findByOrderId(Long orderId);

    /** 查询某用户报名过的订单记录 */
    List<BaseOrderItem> findByUserId(Long userId);

    /** 查询某订单下某用户是否已报名 */
    boolean existsByOrderIdAndUserId(Long orderId, Long userId);

    /**
     * 某零工在某招工类型(daily/heldBack/month)下的报名记录（按订单类型关联过滤）。
     * 用于"我的月结/压薪日结"列表。
     */
    @Query("""
            select i from BaseOrderItem i
            join BossOrder o on i.orderId = o.id
            where i.userId = :userId and o.type = :type
            order by i.id desc
            """)
    List<BaseOrderItem> findByUserIdAndOrderType(@Param("userId") Long userId,
                                                 @Param("type") String type);

    /**
     * 某老板发布的全部订单下的报名记录（用于历史工人/报名总数统计）。
     */
    @Query("""
            select i from BaseOrderItem i
            join BossOrder o on i.orderId = o.id
            where o.createBy = :bossId
            order by i.id desc
            """)
    List<BaseOrderItem> findByBossIdJoinOrder(@Param("bossId") Long bossId);

    /** 某老板发布的全部订单下的报名总数 */
    @Query("""
            select count(i) from BaseOrderItem i
            join BossOrder o on i.orderId = o.id
            where o.createBy = :bossId
            """)
    long countApplicantsByBossId(@Param("bossId") Long bossId);
}

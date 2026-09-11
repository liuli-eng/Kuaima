package com.kuaima.app.domain.boss.repository;

import java.util.List;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.boss.entity.BaseOrderItem;

public interface BaseOrderItemRespository extends JpaRepository<BaseOrderItem, Long> {

    /** 查询某订单的所有报名记录 */
    List<BaseOrderItem> findByOrderId(Long orderId);

    /** 后台订单列表分页：按报名状态过滤 + 分页（status 为空时返回全部） */
    Page<BaseOrderItem> findByStatus(String status, Pageable pageable);

    /** 查询某用户报名过的订单记录 */
    List<BaseOrderItem> findByUserId(Long userId);

    /** 零工订单聚合列表：报名记录与订单类型、状态一起分页查询。 */
    @Query("""
            select i from BaseOrderItem i
            join BossOrder o on i.orderId = o.id
            where i.userId = :userId
              and (:type is null or o.type = :type)
              and (:status is null or i.status = :status)
            order by i.id desc
            """)
    Page<BaseOrderItem> findWorkerOrders(@Param("userId") Long userId,
                                         @Param("type") String type,
                                         @Param("status") String status,
                                         Pageable pageable);

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

    /**
     * 某老板已经完成或已经支付结算的合作记录，按最近合作时间倒序。
     * 结算记录使用实体名 Settlement，避免仅依赖报名记录状态造成已结算历史遗漏。
     */
    @Query("""
            select i from BaseOrderItem i
            join BossOrder o on i.orderId = o.id
            where o.createBy = :bossId
              and (i.status = '已完成'
                   or exists (select s.id from Settlement s
                              where s.itemId = i.id
                                and s.status = '已支付'))
            order by coalesce(i.finishDate, i.workDate, i.hireDate, i.applyDate) desc, i.id desc
            """)
    List<BaseOrderItem> findHistoryByBossId(@Param("bossId") Long bossId);

    /** 某老板发布的全部订单下的报名总数 */
    @Query("""
            select count(i) from BaseOrderItem i
            join BossOrder o on i.orderId = o.id
            where o.createBy = :bossId
            """)
    long countApplicantsByBossId(@Param("bossId") Long bossId);

    /** 按订单批量统计有效报名数，避免岗位列表逐单查询 */
    @Query("""
            select i.orderId, count(i)
            from BaseOrderItem i
            where i.orderId in :orderIds and i.status in :statuses
            group by i.orderId
            """)
    List<Object[]> countByOrderIdsAndStatuses(@Param("orderIds") Collection<Long> orderIds,
                                              @Param("statuses") Collection<String> statuses);

    /** 按零工批量统计已完成订单数 */
    @Query("""
            select i.userId, count(i)
            from BaseOrderItem i
            where i.userId in :userIds and i.status = '已完成'
            group by i.userId
            """)
    List<Object[]> countCompletedByUserIds(@Param("userIds") Collection<Long> userIds);
}

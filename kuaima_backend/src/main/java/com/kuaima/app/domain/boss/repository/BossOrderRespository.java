package com.kuaima.app.domain.boss.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.boss.entity.BossOrder;

public interface BossOrderRespository extends JpaRepository<BossOrder, Long> {

    /** 按 类型/状态/标题 组合过滤分页查询（参数为空表示不过滤） */
    @Query("""
            select o from BossOrder o
            where (:type is null or o.type = :type)
              and (:status is null or o.orderStatus = :status)
              and (:title is null or o.orderTitle like concat('%', :title, '%'))
            """)
    Page<BossOrder> search(@Param("type") String type,
                                @Param("status") String status,
                                @Param("title") String title,
                                Pageable pageable);

    /** 某老板发布的全部订单（最新在前） */
    List<BossOrder> findByCreateByOrderByIdDesc(Long createBy);

    /** 某老板的草稿订单列表 */
    List<BossOrder> findByOrderStatusAndCreateByOrderByIdDesc(String orderStatus, Long createBy);

    /** 高级筛选：城市(address like)/薪资范围/标签/类型/工作时长 */
    @Query("""
            select o from BossOrder o
            where o.orderStatus = '招工中'
              and (:city is null or o.address like concat('%', :city, '%'))
              and (:salaryMin is null or o.salary >= :salaryMin)
              and (:salaryMax is null or o.salary <= :salaryMax)
              and (:tag is null or o.tags like concat('%', :tag, '%'))
              and (:type is null or o.type = :type)
              and (:duration is null or o.duration = :duration)
            order by o.id desc
            """)
    Page<BossOrder> filter(@Param("city") String city,
                           @Param("salaryMin") Integer salaryMin,
                           @Param("salaryMax") Integer salaryMax,
                           @Param("tag") String tag,
                           @Param("type") String type,
                           @Param("duration") Integer duration,
                           Pageable pageable);

    /** 工种分类：所有岗位 distinct 值 */
    @Query("select distinct o.postion from BossOrder o where o.postion is not null")
    List<String> findDistinctPositions();
}

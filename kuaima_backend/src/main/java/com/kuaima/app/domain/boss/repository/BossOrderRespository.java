package com.kuaima.app.domain.boss.repository;

import java.util.Collection;
import java.util.List;
import java.util.Date;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.boss.entity.BossOrder;

public interface BossOrderRespository extends JpaRepository<BossOrder, Long>, JpaSpecificationExecutor<BossOrder> {

    /** 按 类型/状态/标题/雇主 组合过滤分页查询（参数为空表示不过滤） */
    @Query("""
            select o from BossOrder o
              left join User u on u.id = o.createBy
            where (:type is null or o.type = :type)
              and (:status is null or o.orderStatus = :status)
              and (:startDate is null or o.date >= :startDate)
              and (:endDate is null or o.date <= :endDate)
              and (:title is null or lower(coalesce(o.orderTitle, '')) like lower(concat('%', :title, '%'))
                   or lower(coalesce(u.companyName, '')) like lower(concat('%', :title, '%'))
                   or lower(coalesce(u.nickname, '')) like lower(concat('%', :title, '%'))
                   or lower(coalesce(u.username, '')) like lower(concat('%', :title, '%')))
            """)
    Page<BossOrder> search(@Param("type") String type,
                                @Param("status") String status,
                                @Param("title") String title,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                Pageable pageable);

    /** 老板端岗位列表：强制按创建人隔离数据 */
    @Query("""
            select o from BossOrder o
            where o.createBy = :createBy
              and (:type is null or o.type = :type)
              and (:status is null or o.orderStatus = :status)
              and (:title is null or o.orderTitle like concat('%', :title, '%'))
            """)
    Page<BossOrder> searchByCreateBy(@Param("createBy") Long createBy,
                                     @Param("type") String type,
                                     @Param("status") String status,
                                     @Param("title") String title,
                                     Pageable pageable);

    /** 某老板发布的全部订单（最新在前） */
    List<BossOrder> findByCreateByOrderByIdDesc(Long createBy);

    /** 批量读取老板最近发布的岗位，用于后台老板列表展示工种。 */
    List<BossOrder> findByCreateByInOrderByIdDesc(Collection<Long> createBys);

    /** 根据发布时选择的工种分类或历史岗位文本查找老板。 */
    @Query(value = """
            select distinct o.create_by
            from boss_order o
            left join job_category j
              on j.id = o.job_category_id
              or find_in_set(cast(j.id as char), coalesce(o.job_ids, '')) > 0
            where lower(coalesce(j.name, '')) like lower(concat('%', :jobType, '%'))
               or lower(coalesce(o.postion, '')) like lower(concat('%', :jobType, '%'))
            """, nativeQuery = true)
    List<Long> findOwnerIdsByJobType(@Param("jobType") String jobType);

    /** 按岗位最近发布时选择的行业名称筛选老板。 */
    @Query(value = """
            select distinct o.create_by
            from boss_order o
            left join job_industry i on i.id = o.industry_id
            where lower(coalesce(i.name, '')) like lower(concat('%', :industry, '%'))
            """, nativeQuery = true)
    List<Long> findOwnerIdsByIndustry(@Param("industry") String industry);

    List<BossOrder> findByCreateByAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByIdDesc(
            Long createBy, Date startInclusive, Date endExclusive);

    @Query("select o from BossOrder o where o.createBy in :owners and o.startTime < :endExclusive and (o.endTime is null or o.endTime >= :startInclusive) order by o.id desc")
    List<BossOrder> findByCreateByInAndOverlappingTime(@Param("owners") Collection<Long> owners,
                                                        @Param("startInclusive") Date startInclusive,
                                                        @Param("endExclusive") Date endExclusive);

    @Query("select o from BossOrder o where o.enterpriseId = :enterpriseId and o.startTime < :endExclusive and (o.endTime is null or o.endTime >= :startInclusive) order by o.id desc")
    List<BossOrder> findByEnterpriseIdAndOverlappingTime(@Param("enterpriseId") Long enterpriseId,
                                                          @Param("startInclusive") Date startInclusive,
                                                          @Param("endExclusive") Date endExclusive);

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

    /** 按老板批量统计招工数 */
    @Query("""
            select o.createBy, count(o)
            from BossOrder o
            where o.createBy in :createByIds
            group by o.createBy
            """)
    List<Object[]> countByCreateByIds(@Param("createByIds") Collection<Long> createByIds);

}

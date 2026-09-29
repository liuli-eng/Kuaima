package com.kuaima.app.domain.user.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.kuaima.app.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByOpenid(String openid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id=:id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    /** 按手机号查询（可能多个用户共用同一手机号） */
    List<User> findByPhone(String phone);

    /** 查询尚未绑定微信、可登录老板端的授权员工子账号。 */
    @Query("""
            select u from User u
            where u.phone = :phone
              and u.parentUserId is not null
              and coalesce(u.status, '') = '正常'
              and u.openid is null
            """)
    List<User> findUnboundActiveSubAccountsByPhone(@Param("phone") String phone);

    /** 按角色查询（角色: BOSS/USER） */
    List<User> findByRole(String role);

    /** 按角色计数 */
    long countByRole(String role);

    long countByRoleAndCity(String role, String city);


    /** 按角色分页查询 */
    Page<User> findByRole(String role, Pageable pageable);

    /** 按 角色/状态/关键词 组合过滤分页查询 */
    @Query("""
            select u from User u
            where u.role = :role
              and (:status is null or u.status = :status)
              and ((:keyword is null) or (u.username like %:keyword%) or (u.nickname like %:keyword%) or (u.phone like %:keyword%) or (u.companyName like %:keyword%))
            """)
    Page<User> searchByRole(@Param("role") String role,
                            @Param("status") String status,
                            @Param("keyword") String keyword,
                            Pageable pageable);

    /** 后台零工列表：支持注册日期范围筛选。 */
    @Query("""
            select u from User u
            where not (upper(coalesce(u.enterpriseStatus, '')) = 'APPROVED'
                       or (upper(coalesce(u.certType, '')) = 'ENTERPRISE' and u.certStatus = '已通过'))
              and (:status is null or u.status = :status)
              and (:startDate is null or u.date >= :startDate)
              and (:endDate is null or u.date <= :endDate)
              and ((:keyword is null) or (u.username like %:keyword%) or (u.nickname like %:keyword%) or (u.phone like %:keyword%) or (u.companyName like %:keyword%))
            """)
    Page<User> searchWorkers(@Param("role") String role,
                             @Param("status") String status,
                             @Param("keyword") String keyword,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             Pageable pageable);

    /** 后台零工在线/离线筛选：由管理端会话管理器提供目标用户 ID 集合。 */
    @Query("""
            select u from User u
            where not (upper(coalesce(u.enterpriseStatus, '')) = 'APPROVED'
                       or (upper(coalesce(u.certType, '')) = 'ENTERPRISE' and u.certStatus = '已通过'))
              and u.id in :ids
              and (:startDate is null or u.date >= :startDate)
              and (:endDate is null or u.date <= :endDate)
              and ((:keyword is null) or (u.username like %:keyword%) or (u.nickname like %:keyword%) or (u.phone like %:keyword%) or (u.companyName like %:keyword%))
            """)
    Page<User> searchWorkersByIds(@Param("role") String role,
                                  @Param("ids") List<Long> ids,
                                  @Param("keyword") String keyword,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  Pageable pageable);

    @Query("select u.id from User u where not (upper(coalesce(u.enterpriseStatus, '')) = 'APPROVED' or (upper(coalesce(u.certType, '')) = 'ENTERPRISE' and u.certStatus = '已通过'))")
    List<Long> findWorkerIdentityIds();

    long countByRoleAndStatus(String role, String status);

    long countByRoleAndDateBetween(String role, LocalDate startDate, LocalDate endDate);

    /** 使用数据库当前日期统计本月新增，避免应用服务器与数据库时区不一致。 */
    @Query(value = """
            select count(*) from sys_user u
            where u.role = :role
              and u.`date` >= date_format(current_date, '%Y-%m-01')
              and u.`date` < date_add(date_format(current_date, '%Y-%m-01'), interval 1 month)
            """, nativeQuery = true)
    long countCurrentMonthByRole(@Param("role") String role);

    long countByRoleAndDateLessThanEqual(String role, LocalDate date);

    /** 按业务身份统计老板：企业认证通过才算老板，兼容历史认证字段。 */
    @Query("select count(u) from User u where upper(coalesce(u.enterpriseStatus, '')) = 'APPROVED' or (upper(coalesce(u.certType, '')) = 'ENTERPRISE' and u.certStatus = '已通过')")
    long countBossIdentities();

    @Query("select u from User u where (:role is null or u.role = :role) and (:keyword is null or u.username like concat('%', :keyword, '%') or u.nickname like concat('%', :keyword, '%') or u.phone like concat('%', :keyword, '%') or u.companyName like concat('%', :keyword, '%'))")
    Page<User> searchRecipients(@Param("role") String role, @Param("keyword") String keyword, Pageable pageable);

    @Query("select u from User u where (u.enterpriseStatus = 'APPROVED' or (upper(coalesce(u.certType,'')) = 'ENTERPRISE' and u.certStatus = '已通过')) and (:keyword is null or u.username like concat('%', :keyword, '%') or u.nickname like concat('%', :keyword, '%') or u.phone like concat('%', :keyword, '%') or u.companyName like concat('%', :keyword, '%'))")
    Page<User> searchBossIdentityRecipients(@Param("keyword") String keyword, Pageable pageable);

    @Query("select u from User u where not (u.enterpriseStatus = 'APPROVED' or (upper(coalesce(u.certType,'')) = 'ENTERPRISE' and u.certStatus = '已通过')) and (:keyword is null or u.username like concat('%', :keyword, '%') or u.nickname like concat('%', :keyword, '%') or u.phone like concat('%', :keyword, '%') or u.companyName like concat('%', :keyword, '%'))")
    Page<User> searchWorkerIdentityRecipients(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            select u from User u
            where coalesce(u.enterpriseStatus, '') <> 'APPROVED'
              and not (upper(coalesce(u.certType, '')) = 'ENTERPRISE' and coalesce(u.certStatus, '') = '已通过')
              and (:keyword is null or u.username like concat('%', :keyword, '%') or u.nickname like concat('%', :keyword, '%') or u.phone like concat('%', :keyword, '%') or u.skills like concat('%', :keyword, '%'))
              and (:category is null or u.skills like concat('%', :category, '%'))
              and (:minYears is null or coalesce(u.workYears, 0) >= :minYears)
              and (:maxYears is null or coalesce(u.workYears, 0) <= :maxYears)
              and (:favoriteOnly = false or exists (select f.id from TalentFavorite f where f.workerId = u.id and f.bossId = :bossId))
            """)
    Page<User> searchTalents(@Param("keyword") String keyword,
                             @Param("category") String category,
                             @Param("minYears") Integer minYears,
                             @Param("maxYears") Integer maxYears,
                             @Param("favoriteOnly") boolean favoriteOnly,
                             @Param("bossId") Long bossId,
                             Pageable pageable);

    /**
     * 按老板业务身份/状态/企业认证状态/行业/关键词分页查询。
     *
     * 老板业务身份以企业认证通过为准，不能仅凭用户当前 role=BOSS 判断；
     * 同时兼容历史数据中 certType=ENTERPRISE、certStatus=已通过的记录。
     */
    @Query("""
            select u from User u
            where (upper(coalesce(u.enterpriseStatus, '')) = 'APPROVED'
                   or (upper(coalesce(u.certType, '')) = 'ENTERPRISE' and u.certStatus = '已通过'))
              and (:status is null or u.status = :status)
              and (:enterpriseStatus is null or u.enterpriseStatus = :enterpriseStatus)
              and (:industry is null or u.industry = :industry)
              and ((:keyword is null) or (u.companyCode like %:keyword%) or (u.companyName like %:keyword%) or (u.username like %:keyword%) or (u.nickname like %:keyword%) or (u.phone like %:keyword%))
            """)
    Page<User> searchBosses(@Param("role") String role,
                            @Param("status") String status,
                            @Param("enterpriseStatus") String enterpriseStatus,
                            @Param("industry") String industry,
                            @Param("keyword") String keyword,
                            Pageable pageable);
}

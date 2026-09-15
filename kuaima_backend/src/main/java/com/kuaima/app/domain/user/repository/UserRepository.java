package com.kuaima.app.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByOpenid(String openid);

    /** 按手机号查询（可能多个用户共用同一手机号） */
    List<User> findByPhone(String phone);

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

    @Query("""
            select u from User u
            where u.role = 'USER'
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

    /** 按 角色/状态/企业认证状态/关键词 组合过滤分页查询（用于雇主列表企业认证筛选） */
    @Query("""
            select u from User u
            where u.role = :role
              and (:status is null or u.status = :status)
              and (:enterpriseStatus is null or u.enterpriseStatus = :enterpriseStatus)
              and ((:keyword is null) or (u.username like %:keyword%) or (u.nickname like %:keyword%) or (u.phone like %:keyword%) or (u.companyName like %:keyword%))
            """)
    Page<User> searchBosses(@Param("role") String role,
                            @Param("status") String status,
                            @Param("enterpriseStatus") String enterpriseStatus,
                            @Param("keyword") String keyword,
                            Pageable pageable);
}

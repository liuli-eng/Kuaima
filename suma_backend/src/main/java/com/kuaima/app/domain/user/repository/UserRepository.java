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

    /** 按角色查询（角色: BOSS/USER） */
    List<User> findByRole(String role);

    /** 按角色计数 */
    long countByRole(String role);

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
}

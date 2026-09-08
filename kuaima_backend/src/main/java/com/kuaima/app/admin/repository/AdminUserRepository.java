package com.kuaima.app.admin.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.admin.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    boolean existsByUsername(String username);

    /**
     * 非超级管理员只能看到自己的账号 + 自己创建的账号
     */
    @Query("SELECT a FROM AdminUser a WHERE a.id = :adminId OR a.createdBy = :adminId")
    Page<AdminUser> findVisibleByAdmin(@Param("adminId") Long adminId, Pageable pageable);
}

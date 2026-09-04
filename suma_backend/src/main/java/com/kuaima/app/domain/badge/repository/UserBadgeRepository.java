package com.kuaima.app.domain.badge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.badge.entity.UserBadge;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    /** 按用户查已解锁徽章 */
    List<UserBadge> findByUserId(Long userId);
}

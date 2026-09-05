package com.kuaima.app.domain.badge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.badge.entity.Badge;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    /** 查询全部徽章（沿用默认 findAll，此处显式声明便于阅读） */
    @Override
    List<Badge> findAll();
}

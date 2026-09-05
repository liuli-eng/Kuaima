package com.kuaima.app.domain.points.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.points.entity.PointsFlow;

public interface PointsFlowRepository extends JpaRepository<PointsFlow, Long> {

    /** 按用户查积分流水（按 timestamp 倒序） */
    List<PointsFlow> findByUserIdOrderByTimestampDesc(Long userId);

    /** 按用户分页查积分流水（按 timestamp 倒序） */
    Page<PointsFlow> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
}

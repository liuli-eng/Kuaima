package com.kuaima.app.domain.points.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.points.entity.PointsFlow;

public interface PointsFlowRepository extends JpaRepository<PointsFlow, Long> {

    /** 按用户 + 身份查积分流水（按 timestamp 倒序）。 */
    List<PointsFlow> findByUserIdAndRoleOrderByTimestampDesc(Long userId, String role);

    /** 按用户 + 身份分页查积分流水（按 timestamp 倒序）。 */
    Page<PointsFlow> findByUserIdAndRoleOrderByTimestampDesc(Long userId, String role, Pageable pageable);

    @Query("select f from PointsFlow f where f.userId=:userId and f.role=:role and (:type='ALL' or f.bizType=:type) order by f.timestamp desc")
    Page<PointsFlow> findByUserIdAndRoleAndType(
            @Param("userId") Long userId,
            @Param("role") String role,
            @Param("type") String type,
            Pageable pageable);
}

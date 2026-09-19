package com.kuaima.app.domain.position.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kuaima.app.domain.position.entity.BossPosition;

@Repository
public interface BossPositionRepository extends JpaRepository<BossPosition, Long> {

    Optional<BossPosition> findByCode(String code);

    boolean existsByCode(String code);

    long countByStatus(String status);

    @Query("SELECT p FROM BossPosition p WHERE " +
           "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.code LIKE %:keyword%) AND " +
           "(:status IS NULL OR p.status = :status) " +
           "ORDER BY p.timestamp DESC")
    Page<BossPosition> searchByKeywordAndStatus(@Param("keyword") String keyword,
                                                 @Param("status") String status,
                                                 Pageable pageable);

    @Query("SELECT p FROM BossPosition p WHERE p.status = 'on' ORDER BY p.hotCount DESC")
    List<BossPosition> findTopByHotCount(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.applyCount), 0) FROM BossPosition p WHERE p.status = 'on'")
    Long sumApplyCount();

    @Query("SELECT COALESCE(SUM(p.interviewCount), 0) FROM BossPosition p WHERE p.status = 'on'")
    Long sumInterviewCount();

    @Query("SELECT COALESCE(SUM(p.hiredCount), 0) FROM BossPosition p WHERE p.status = 'on'")
    Long sumHiredCount();

    @Query("SELECT COALESCE(SUM(p.hireCount), 0) FROM BossPosition p WHERE p.status = 'on'")
    Long sumHireCount();
}

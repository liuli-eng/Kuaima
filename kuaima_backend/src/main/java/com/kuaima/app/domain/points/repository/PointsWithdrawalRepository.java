package com.kuaima.app.domain.points.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.points.entity.PointsWithdrawal;

import jakarta.persistence.LockModeType;

public interface PointsWithdrawalRepository extends JpaRepository<PointsWithdrawal, Long> {
    Optional<PointsWithdrawal> findByIdempotencyKey(String key);
    Page<PointsWithdrawal> findByUserIdAndRoleOrderByAppliedAtDescIdDesc(Long userId, String role, Pageable pageable);
    Page<PointsWithdrawal> findByUserIdAndRoleAndStatusOrderByAppliedAtDescIdDesc(Long userId, String role, String status, Pageable pageable);
    Page<PointsWithdrawal> findByStatusOrderByAppliedAtAscIdAsc(String status, Pageable pageable);
    Page<PointsWithdrawal> findAllByOrderByAppliedAtDescIdDesc(Pageable pageable);
    Page<PointsWithdrawal> findAllByStatusOrderByAppliedAtDescIdDesc(String status, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from PointsWithdrawal w where w.id = :id")
    Optional<PointsWithdrawal> findByIdForUpdate(@Param("id") Long id);
    @Query("select count(w) from PointsWithdrawal w where w.userId=:userId and w.role='USER' and w.status in :statuses and w.appliedAt >= :from and w.appliedAt < :to")
    long countValidToday(@Param("userId") Long userId, @Param("statuses") java.util.Collection<String> statuses,
                         @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

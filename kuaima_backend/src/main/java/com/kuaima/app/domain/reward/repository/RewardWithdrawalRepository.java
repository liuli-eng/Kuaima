package com.kuaima.app.domain.reward.repository;

import com.kuaima.app.domain.reward.entity.RewardWithdrawal;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardWithdrawalRepository extends JpaRepository<RewardWithdrawal, Long> {
    Optional<RewardWithdrawal> findByIdempotencyKey(String idempotencyKey);
    Page<RewardWithdrawal> findByUserIdOrderByAppliedAtDescIdDesc(Long userId, Pageable pageable);
    Page<RewardWithdrawal> findByUserIdAndRoleOrderByAppliedAtDescIdDesc(Long userId, String role, Pageable pageable);
    List<RewardWithdrawal> findTop20ByStatusAndAppliedAtBeforeOrderByAppliedAtAscIdAsc(String status, LocalDateTime time);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from RewardWithdrawal w where w.id=:id")
    Optional<RewardWithdrawal> findByIdForUpdate(@Param("id") Long id);
}

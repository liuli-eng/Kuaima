package com.kuaima.app.domain.reward.repository;

import com.kuaima.app.domain.reward.entity.RewardWithdrawal;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardWithdrawalRepository extends JpaRepository<RewardWithdrawal, Long> {
    Optional<RewardWithdrawal> findByIdempotencyKey(String idempotencyKey);
    Page<RewardWithdrawal> findByUserIdOrderByAppliedAtDescIdDesc(Long userId, Pageable pageable);
}

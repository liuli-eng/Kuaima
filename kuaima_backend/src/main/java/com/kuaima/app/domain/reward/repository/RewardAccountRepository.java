package com.kuaima.app.domain.reward.repository;

import com.kuaima.app.domain.reward.entity.RewardAccount;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RewardAccountRepository extends JpaRepository<RewardAccount, Long> {
    Optional<RewardAccount> findByUserId(Long userId);
    List<RewardAccount> findByUserIdIn(Collection<Long> userIds);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from RewardAccount a where a.userId=:userId")
    Optional<RewardAccount> findByUserIdForUpdate(@Param("userId") Long userId);
}

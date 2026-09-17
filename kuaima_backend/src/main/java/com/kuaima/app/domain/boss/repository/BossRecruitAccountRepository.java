package com.kuaima.app.domain.boss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.boss.entity.BossRecruitAccount;

public interface BossRecruitAccountRepository extends JpaRepository<BossRecruitAccount, Long> {
    List<BossRecruitAccount> findByOwnerUserId(Long id);

    List<BossRecruitAccount> findByOwnerUserIdOrderByIdAsc(Long id);

    Optional<BossRecruitAccount> findByIdAndOwnerUserId(Long id, Long owner);

    Optional<BossRecruitAccount> findByOwnerUserIdAndTargetUserId(Long ownerUserId, Long targetUserId);

    Optional<BossRecruitAccount> findByOwnerUserIdAndCurrentTrue(Long ownerUserId);

    boolean existsByOwnerUserIdAndTargetUserId(Long ownerUserId, Long targetUserId);
}

package com.kuaima.app.domain.boss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.boss.entity.BossMerchantAccount;

public interface BossMerchantAccountRepository extends JpaRepository<BossMerchantAccount, Long> {

    List<BossMerchantAccount> findByBossIdOrderByIdDesc(Long bossId);

    Optional<BossMerchantAccount> findFirstByBossIdAndIsDefaultTrue(Long bossId);
}

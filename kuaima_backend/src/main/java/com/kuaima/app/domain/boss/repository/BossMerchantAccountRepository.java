package com.kuaima.app.domain.boss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.boss.entity.BossMerchantAccount;

import jakarta.persistence.LockModeType;

public interface BossMerchantAccountRepository extends JpaRepository<BossMerchantAccount, Long> {

    List<BossMerchantAccount> findByBossIdOrderByIdDesc(Long bossId);

    Optional<BossMerchantAccount> findFirstByBossIdAndIsDefaultTrue(Long bossId);

    boolean existsByBossId(Long bossId);

    /** 扣款前锁定默认账户：防止并发扣款导致余额覆盖写。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BossMerchantAccount a where a.bossId = :bossId and a.isDefault = true order by a.id")
    List<BossMerchantAccount> findByBossIdAndIsDefaultTrueForUpdate(@Param("bossId") Long bossId);

    /** 无默认账户时的兜底：按 id 倒序取最新账户并加锁。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BossMerchantAccount a where a.bossId = :bossId order by a.id desc")
    List<BossMerchantAccount> findByBossIdForUpdate(@Param("bossId") Long bossId);
}
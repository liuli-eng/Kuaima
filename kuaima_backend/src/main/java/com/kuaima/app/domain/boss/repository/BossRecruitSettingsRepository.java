package com.kuaima.app.domain.boss.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kuaima.app.domain.boss.entity.BossRecruitSettings;

public interface BossRecruitSettingsRepository extends JpaRepository<BossRecruitSettings, Long> {
    Optional<BossRecruitSettings> findByBossId(Long bossId);
    Optional<BossRecruitSettings> findByEnterpriseId(Long enterpriseId);
}

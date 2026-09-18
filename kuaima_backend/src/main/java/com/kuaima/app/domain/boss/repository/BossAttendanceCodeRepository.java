package com.kuaima.app.domain.boss.repository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kuaima.app.domain.boss.entity.BossAttendanceCode;
public interface BossAttendanceCodeRepository extends JpaRepository<BossAttendanceCode, Long> {
 Optional<BossAttendanceCode> findByBossIdAndCodeDate(Long bossId, LocalDate date);
 Optional<BossAttendanceCode> findByEnterpriseIdAndCodeDate(Long enterpriseId, LocalDate date);
}

package com.kuaima.app.domain.resume.repository;

import com.kuaima.app.domain.resume.entity.ResumeImportRecord;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeImportRecordRepository extends JpaRepository<ResumeImportRecord, Long> {

    List<ResumeImportRecord> findTop20ByBossIdOrderByIdDesc(Long bossId);
}

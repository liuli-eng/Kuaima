package com.kuaima.app.domain.talentpool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kuaima.app.domain.talentpool.entity.TalentHire;

@Repository
public interface TalentHireRepository extends JpaRepository<TalentHire, Long> {

    List<TalentHire> findByBossIdOrderByIdDesc(Long bossId);

    List<TalentHire> findByTalentIdOrderByIdDesc(Long talentId);
}

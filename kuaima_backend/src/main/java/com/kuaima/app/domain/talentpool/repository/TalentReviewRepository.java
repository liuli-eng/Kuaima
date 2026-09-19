package com.kuaima.app.domain.talentpool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kuaima.app.domain.talentpool.entity.TalentReview;

@Repository
public interface TalentReviewRepository extends JpaRepository<TalentReview, Long> {

    List<TalentReview> findByTalentIdOrderByIdDesc(Long talentId);
}

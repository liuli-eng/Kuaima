package com.kuaima.app.domain.resume.repository;

import com.kuaima.app.domain.resume.entity.ResumeExperience;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, Long> {

    List<ResumeExperience> findByResumeIdOrderByIdAsc(Long resumeId);

    void deleteByResumeId(Long resumeId);
}

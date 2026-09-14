package com.kuaima.app.domain.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.project.entity.OnboardApply;

public interface OnboardRepository extends JpaRepository<OnboardApply, Long>, JpaSpecificationExecutor<OnboardApply> {

    List<OnboardApply> findByProjectId(Long projectId);

    List<OnboardApply> findByProjectIdAndStatus(Long projectId, String status);
}

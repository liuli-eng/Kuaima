package com.kuaima.app.domain.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.project.entity.ProjectMember;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long>, JpaSpecificationExecutor<ProjectMember> {

    List<ProjectMember> findByProjectId(Long projectId);

    List<ProjectMember> findByProjectIdAndStatus(Long projectId, String status);
}

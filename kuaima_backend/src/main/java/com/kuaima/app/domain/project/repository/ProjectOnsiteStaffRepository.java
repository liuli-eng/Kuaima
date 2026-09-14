package com.kuaima.app.domain.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.project.entity.ProjectOnsiteStaff;

public interface ProjectOnsiteStaffRepository extends JpaRepository<ProjectOnsiteStaff, Long>,
        JpaSpecificationExecutor<ProjectOnsiteStaff> {

    List<ProjectOnsiteStaff> findByProjectId(Long projectId);
}

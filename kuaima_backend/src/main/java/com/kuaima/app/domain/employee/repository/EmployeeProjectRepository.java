package com.kuaima.app.domain.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.employee.entity.EmployeeProject;

public interface EmployeeProjectRepository extends JpaRepository<EmployeeProject, Long>,
        JpaSpecificationExecutor<EmployeeProject> {

    List<EmployeeProject> findByEmployeeId(Long employeeId);

    List<EmployeeProject> findByProjectId(Long projectId);

    Optional<EmployeeProject> findByEmployeeIdAndProjectId(Long employeeId, Long projectId);
}

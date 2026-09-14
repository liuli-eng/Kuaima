package com.kuaima.app.domain.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.employee.entity.EmployeeRole;

public interface EmployeeRoleRepository extends JpaRepository<EmployeeRole, Long>,
        JpaSpecificationExecutor<EmployeeRole> {

    Optional<EmployeeRole> findByName(String name);
}

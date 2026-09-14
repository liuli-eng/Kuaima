package com.kuaima.app.domain.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    List<Employee> findByCompany(String company);

    List<Employee> findByRole(String role);

    List<Employee> findByStatus(String status);

    Optional<Employee> findByPhone(String phone);
}

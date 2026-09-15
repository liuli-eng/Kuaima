package com.kuaima.app.domain.employee.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.employee.constant.EmployeeConstants;
import com.kuaima.app.domain.employee.entity.Employee;
import com.kuaima.app.domain.employee.repository.EmployeeRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Page<Employee> list(String keyword, String company, String role, String status, Pageable pageable) {
        Specification<Employee> spec = buildSpec(keyword, company, role, status);
        return employeeRepository.findAll(spec, pageable);
    }

    /**
     * 统计员工数（用于首页卡片）。参数均为可选过滤。
     */
    public long count(String keyword, String company, String role, String status) {
        Specification<Employee> spec = buildSpec(keyword, company, role, status);
        return employeeRepository.count(spec);
    }

    private Specification<Employee> buildSpec(String keyword, String company, String role, String status) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(company) && !"all".equals(company)) {
                predicates.add(cb.equal(root.get("company"), company));
            }
            if (StringUtils.hasText(role) && !"all".equals(role)) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (StringUtils.hasText(status) && !"all".equals(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("phone"), like)));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public Employee getOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("员工不存在: " + id));
    }

    @Transactional
    public Employee create(Employee employee, Long operatorId) {
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeConstants.EMP_ACTIVE);
        }
        if (operatorId != null) {
            employee.setCreateBy(operatorId);
        }
        if (employee.getAddTime() == null) {
            employee.setAddTime(new java.util.Date());
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(Long id, Employee source) {
        Employee employee = getOrThrow(id);
        if (StringUtils.hasText(source.getName())) {
            employee.setName(source.getName());
        }
        if (StringUtils.hasText(source.getPhone())) {
            employee.setPhone(source.getPhone());
        }
        if (StringUtils.hasText(source.getJob())) {
            employee.setJob(source.getJob());
        }
        if (StringUtils.hasText(source.getCompany())) {
            employee.setCompany(source.getCompany());
        }
        if (StringUtils.hasText(source.getRole())) {
            employee.setRole(source.getRole());
        }
        if (StringUtils.hasText(source.getRoleName())) {
            employee.setRoleName(source.getRoleName());
        }
        if (source.getRoleId() != null) {
            employee.setRoleId(source.getRoleId());
        }
        if (StringUtils.hasText(source.getPermissions())) {
            employee.setPermissions(source.getPermissions());
        }
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee setStatus(Long id, String status) {
        Employee employee = getOrThrow(id);
        employee.setStatus(status);
        return employeeRepository.save(employee);
    }

    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EntityNotFoundException("员工不存在: " + id);
        }
        employeeRepository.deleteById(id);
    }

    /** 员工列表（用于驻场添加时的企业员工池）。 */
    public List<Employee> allEmployees() {
        return employeeRepository.findAll();
    }
}

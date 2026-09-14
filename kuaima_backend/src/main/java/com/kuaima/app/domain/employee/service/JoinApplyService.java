package com.kuaima.app.domain.employee.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.employee.constant.EmployeeConstants;
import com.kuaima.app.domain.employee.entity.Employee;
import com.kuaima.app.domain.employee.entity.JoinApply;
import com.kuaima.app.domain.employee.repository.EmployeeRepository;
import com.kuaima.app.domain.employee.repository.JoinApplyRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class JoinApplyService {

    private final JoinApplyRepository joinApplyRepository;
    private final EmployeeRepository employeeRepository;

    public JoinApplyService(JoinApplyRepository joinApplyRepository, EmployeeRepository employeeRepository) {
        this.joinApplyRepository = joinApplyRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<JoinApply> list(String status) {
        if (EmployeeConstants.APPLY_PENDING.equals(status)
                || EmployeeConstants.APPLY_APPROVED.equals(status)
                || EmployeeConstants.APPLY_REJECTED.equals(status)) {
            return joinApplyRepository.findByStatus(status);
        }
        return joinApplyRepository.findAll();
    }

    public JoinApply getOrThrow(Long id) {
        return joinApplyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("加入申请不存在: " + id));
    }

    @Transactional
    public JoinApply approve(Long id, String reviewer) {
        JoinApply apply = getOrThrow(id);
        apply.setStatus(EmployeeConstants.APPLY_APPROVED);
        apply.setReviewer(reviewer);
        apply.setReviewTime(new java.util.Date());
        joinApplyRepository.save(apply);

        // 通过后自动创建企业员工
        Employee employee = new Employee();
        employee.setName(apply.getName());
        employee.setPhone(apply.getPhone());
        employee.setJob(apply.getJob());
        employee.setRole(EmployeeConstants.ROLE_STAFF);
        employee.setRoleName(apply.getApplyRole());
        employee.setCompany(apply.getCompany());
        employee.setStatus(EmployeeConstants.EMP_ACTIVE);
        employee.setAddTime(new java.util.Date());
        employeeRepository.save(employee);
        return apply;
    }

    @Transactional
    public JoinApply reject(Long id, String reviewer) {
        JoinApply apply = getOrThrow(id);
        apply.setStatus(EmployeeConstants.APPLY_REJECTED);
        apply.setReviewer(reviewer);
        apply.setReviewTime(new java.util.Date());
        return joinApplyRepository.save(apply);
    }
}

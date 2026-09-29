package com.kuaima.app.domain.employee.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.employee.constant.EmployeeConstants;
import com.kuaima.app.domain.employee.entity.Employee;
import com.kuaima.app.domain.employee.entity.JoinApply;
import com.kuaima.app.domain.employee.repository.EmployeeRepository;
import com.kuaima.app.domain.employee.repository.JoinApplyRepository;
import com.kuaima.app.domain.enterprise.entity.Enterprise;
import com.kuaima.app.domain.enterprise.entity.EnterpriseJoinApply;
import com.kuaima.app.domain.enterprise.repository.EnterpriseJoinApplyRepository;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 加入申请（后台申请列表）。
 * 平台后台同时管理两张申请表：
 * - join_apply：后台表单录入的申请；
 * - enterprise_join_apply：小程序扫码 / 邀请链接产生的入企申请。
 * 列表合并展示，审批按记录来源写回对应表，避免扫码申请在后台不可见。
 */
@Service
public class JoinApplyService {

    private final JoinApplyRepository joinApplyRepository;
    private final EmployeeRepository employeeRepository;
    private final EnterpriseJoinApplyRepository enterpriseApplyRepository;
    private final EnterpriseRepository enterpriseRepository;

    public JoinApplyService(JoinApplyRepository joinApplyRepository,
                            EmployeeRepository employeeRepository,
                            EnterpriseJoinApplyRepository enterpriseApplyRepository,
                            EnterpriseRepository enterpriseRepository) {
        this.joinApplyRepository = joinApplyRepository;
        this.employeeRepository = employeeRepository;
        this.enterpriseApplyRepository = enterpriseApplyRepository;
        this.enterpriseRepository = enterpriseRepository;
    }

    /** 合并列表：join_apply 与 enterprise_join_apply 统一成同一结构，按申请时间倒序。 */
    public List<Map<String, Object>> listMerged(String status) {
        String normalized = normalizeQueryStatus(status);
        List<Map<String, Object>> merged = new ArrayList<>();

        List<JoinApply> forms = normalized == null
                ? joinApplyRepository.findAll()
                : joinApplyRepository.findByStatus(normalized);
        for (JoinApply apply : forms) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", apply.getId());
            item.put("source", "join_apply");
            item.put("name", apply.getName());
            item.put("phone", apply.getPhone());
            item.put("job", apply.getJob());
            item.put("applyRole", apply.getApplyRole());
            item.put("intentProject", apply.getIntentProject());
            item.put("applyTime", formatTime(apply.getApplyTime()));
            item.put("status", normalizeStatus(apply.getStatus()));
            item.put("company", apply.getCompany());
            item.put("salaryExpect", apply.getSalaryExpect());
            item.put("workTime", apply.getWorkTime());
            item.put("joinDate", apply.getJoinDate() != null ? apply.getJoinDate().toString() : "");
            merged.add(item);
        }

        List<EnterpriseJoinApply> scanned = enterpriseApplyRepository.findAll();
        Map<Long, String> companyNames = scanned.stream()
                .map(EnterpriseJoinApply::getEnterpriseId)
                .distinct()
                .collect(Collectors.toMap(id -> id, id -> enterpriseRepository.findById(id)
                        .map(Enterprise::getCompanyName).orElse("")));
        for (EnterpriseJoinApply apply : scanned) {
            String rowStatus = normalizeStatus(apply.getStatus());
            if (normalized != null && !normalized.equals(rowStatus)) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", apply.getId());
            item.put("source", "enterprise_join_apply");
            item.put("name", apply.getName());
            item.put("phone", apply.getPhone());
            item.put("job", "");
            item.put("applyRole", roleText(apply.getApplyRole()));
            item.put("intentProject", "");
            item.put("applyTime", formatTime(apply.getTimestamp()));
            item.put("status", rowStatus);
            item.put("company", companyNames.getOrDefault(apply.getEnterpriseId(), ""));
            item.put("salaryExpect", "");
            item.put("workTime", "");
            item.put("joinDate", apply.getDate() != null ? apply.getDate().toString() : "");
            merged.add(item);
        }

        merged.sort(Comparator.comparing((Map<String, Object> row) -> String.valueOf(row.get("applyTime")))
                .reversed());
        return merged;
    }

    /** 审批小程序扫码申请：通过后同步落一条后台员工记录。 */
    @Transactional
    public EnterpriseJoinApply approveEnterpriseApply(Long id, String reviewer) {
        EnterpriseJoinApply apply = enterpriseApplyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("加入申请不存在: " + id));
        ensurePending(apply.getStatus());
        apply.setStatus("AGREED");
        enterpriseApplyRepository.save(apply);

        Enterprise enterprise = enterpriseRepository.findById(apply.getEnterpriseId()).orElse(null);
        Employee employee = new Employee();
        employee.setName(apply.getName());
        employee.setPhone(apply.getPhone());
        employee.setRole(EmployeeConstants.ROLE_STAFF);
        employee.setRoleName(roleText(apply.getApplyRole()));
        employee.setCompany(enterprise != null ? enterprise.getCompanyName() : null);
        employee.setStatus(EmployeeConstants.EMP_ACTIVE);
        employee.setAddTime(new Date());
        employeeRepository.save(employee);
        return apply;
    }

    @Transactional
    public EnterpriseJoinApply rejectEnterpriseApply(Long id, String reviewer) {
        EnterpriseJoinApply apply = enterpriseApplyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("加入申请不存在: " + id));
        ensurePending(apply.getStatus());
        apply.setStatus("REFUSED");
        return enterpriseApplyRepository.save(apply);
    }

    private void ensurePending(String status) {
        if (!"PENDING".equals(status)) {
            throw new IllegalStateException("该申请已处理");
        }
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

    /** 把 enterprise_join_apply 的状态（PENDING/AGREED/REFUSED）映射为后台列表状态。 */
    private String normalizeStatus(String status) {
        if (status == null) return EmployeeConstants.APPLY_PENDING;
        return switch (status.toUpperCase()) {
            case "APPROVED", "AGREED" -> EmployeeConstants.APPLY_APPROVED;
            case "REJECTED", "REFUSED" -> EmployeeConstants.APPLY_REJECTED;
            default -> EmployeeConstants.APPLY_PENDING;
        };
    }

    /** 请求参数（all/pending/approved/rejected）转成内部状态；all 返回 null 表示不过滤。 */
    private String normalizeQueryStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) return null;
        return switch (status.toLowerCase()) {
            case "pending", "approved", "rejected" -> status.toLowerCase();
            default -> null;
        };
    }

    private String roleText(String applyRole) {
        if (applyRole == null) return "员工";
        return switch (applyRole.toUpperCase()) {
            case "ADMIN" -> "管理员";
            case "OWNER" -> "超级管理员";
            default -> "员工";
        };
    }

    private String formatTime(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}
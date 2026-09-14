package com.kuaima.app.domain.employee.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.employee.constant.EmployeeConstants;
import com.kuaima.app.domain.employee.entity.EmployeeRole;
import com.kuaima.app.domain.employee.repository.EmployeeRoleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeRoleService {

    private final EmployeeRoleRepository roleRepository;

    public EmployeeRoleService(EmployeeRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<EmployeeRole> list() {
        return roleRepository.findAll();
    }

    public EmployeeRole getOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("角色不存在: " + id));
    }

    @Transactional
    public EmployeeRole create(EmployeeRole role, Long operatorId) {
        if (role.getSystem() == null) {
            role.setSystem(false);
        }
        if (operatorId != null) {
            role.setCreateBy(operatorId);
        }
        return roleRepository.save(role);
    }

    @Transactional
    public EmployeeRole update(Long id, EmployeeRole source) {
        EmployeeRole role = getOrThrow(id);
        if (source.getName() != null) {
            role.setName(source.getName());
        }
        if (source.getDescription() != null) {
            role.setDescription(source.getDescription());
        }
        if (source.getColor() != null) {
            role.setColor(source.getColor());
        }
        if (source.getIcon() != null) {
            role.setIcon(source.getIcon());
        }
        if (source.getPermissions() != null) {
            role.setPermissions(source.getPermissions());
        }
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(Long id) {
        EmployeeRole role = getOrThrow(id);
        if (Boolean.TRUE.equals(role.getSystem())) {
            throw new IllegalStateException("系统内置角色不可删除");
        }
        roleRepository.deleteById(id);
    }

    /** 初始化系统内置角色（超级管理员 / 管理员 / 员工）。幂等执行。 */
    @Transactional
    public void initDefaults() {
        if (roleRepository.findByName("超级管理员").isEmpty()) {
            EmployeeRole superRole = new EmployeeRole();
            superRole.setName("超级管理员");
            superRole.setDescription("管理项目、审批发薪单、查看余额流水");
            superRole.setSystem(true);
            superRole.setColor("#FF6B35");
            superRole.setIcon("fa-user-shield");
            superRole.setPermissions("{\"permOrg\":{\"org_member\":true,\"org_project\":true},\"permPayroll\":{\"batch_payroll\":true,\"payroll_approve\":true,\"transfer_record\":true,\"balance_view\":true}}");
            roleRepository.save(superRole);
        }
        if (roleRepository.findByName("管理员").isEmpty()) {
            EmployeeRole adminRole = new EmployeeRole();
            adminRole.setName("管理员");
            adminRole.setDescription("管理项目、审批发薪单、查看余额流水");
            adminRole.setSystem(true);
            adminRole.setColor("#3B82F6");
            adminRole.setIcon("fa-user-shield");
            adminRole.setPermissions("{\"permOrg\":{\"org_member\":true,\"org_project\":true},\"permPayroll\":{\"batch_payroll\":true,\"payroll_approve\":true,\"transfer_record\":true,\"balance_view\":true}}");
            roleRepository.save(adminRole);
        }
        if (roleRepository.findByName("员工").isEmpty()) {
            EmployeeRole staffRole = new EmployeeRole();
            staffRole.setName("员工");
            staffRole.setDescription("执行日常发薪、考勤等操作");
            staffRole.setSystem(true);
            staffRole.setColor("#10B981");
            staffRole.setIcon("fa-user");
            staffRole.setPermissions("{\"permOrg\":{\"org_member\":true},\"permPayroll\":{\"batch_payroll\":true}}");
            roleRepository.save(staffRole);
        }
    }

    /** 默认权限树结构（供前端角色权限编辑初始化）。 */
    public List<Object> defaultPermissionTree() {
        return Arrays.asList(
                java.util.Map.of("key", "permOrg", "name", "组织管理", "children",
                        Arrays.asList(
                                java.util.Map.of("key", "org_member", "name", "成员管理"),
                                java.util.Map.of("key", "org_project", "name", "项目管理"))),
                java.util.Map.of("key", "permPayroll", "name", "闪电发薪", "children",
                        Arrays.asList(
                                java.util.Map.of("key", "batch_payroll", "name", "批量发薪"),
                                java.util.Map.of("key", "payroll_approve", "name", "待我审批"),
                                java.util.Map.of("key", "transfer_record", "name", "转账记录"),
                                java.util.Map.of("key", "balance_view", "name", "余额查询"))));
    }
}

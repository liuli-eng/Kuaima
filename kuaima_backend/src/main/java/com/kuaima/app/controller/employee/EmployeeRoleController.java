package com.kuaima.app.controller.employee;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.employee.entity.EmployeeRole;
import com.kuaima.app.domain.employee.service.EmployeeRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/employee-roles")
@Tag(name = "后台-角色管理", description = "角色 CRUD、权限树、初始化内置角色")
public class EmployeeRoleController {

    private final EmployeeRoleService roleService;

    public EmployeeRoleController(EmployeeRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(summary = "角色列表")
    public Result<List<EmployeeRole>> list() {
        return Result.success(roleService.list());
    }

    @GetMapping("/permission-tree")
    @Operation(summary = "默认权限树（角色权限编辑初始化）")
    public Result<List<Object>> permissionTree() {
        return Result.success(roleService.defaultPermissionTree());
    }

    @PostMapping
    @Operation(summary = "新建角色")
    public Result<EmployeeRole> create(@RequestBody EmployeeRole role) {
        return Result.success(roleService.create(role, null));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑角色")
    public Result<EmployeeRole> update(@PathVariable Long id, @RequestBody EmployeeRole role) {
        return Result.success(roleService.update(id, role));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色（系统内置不可删）")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @PostMapping("/init")
    @Operation(summary = "初始化系统内置角色")
    public Result<Void> init() {
        roleService.initDefaults();
        return Result.success();
    }
}

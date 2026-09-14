package com.kuaima.app.controller.employee;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.employee.entity.Employee;
import com.kuaima.app.domain.employee.service.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/employees")
@Tag(name = "后台-员工管理", description = "企业员工列表、详情、增删、状态（停用/启用）")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "员工列表")
    public Result<List<Employee>> list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Employee> result = employeeService.list(keyword, company, role, status, PageRequest.of(page, size));
        return Result.success(result.getContent(), page, result.getTotalElements());
    }

    @GetMapping("/{id}")
    @Operation(summary = "员工详情")
    public Result<Employee> detail(@PathVariable Long id) {
        return Result.success(employeeService.getOrThrow(id));
    }

    @PostMapping
    @Operation(summary = "新增员工")
    public Result<Employee> create(@RequestBody Employee employee) {
        return Result.success(employeeService.create(employee, null));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑员工")
    public Result<Employee> update(@PathVariable Long id, @RequestBody Employee employee) {
        return Result.success(employeeService.update(id, employee));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "变更员工状态（active/frozen）")
    public Result<Employee> setStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(employeeService.setStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.success();
    }
}

package com.kuaima.app.controller.payroll;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.payroll.entity.PayrollOrder;
import com.kuaima.app.domain.payroll.model.PayrollCreateRequest;
import com.kuaima.app.domain.payroll.service.PayrollService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/payrolls")
@Tag(name = "后台-发薪管理", description = "发薪单列表、详情、创建、提交、审批")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    @Operation(summary = "发薪单列表（tab: submitted/reviewed）")
    public Result<List<PayrollOrder>> list(@RequestParam(required = false, defaultValue = "submitted") String tab,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PayrollOrder> result = payrollService.listOrders(tab, status, projectId, keyword, PageRequest.of(page, size));
        return Result.success(result.getContent(), page, result.getTotalElements());
    }

    @GetMapping("/stats")
    @Operation(summary = "发薪管理统计")
    public Result<Map<String, Object>> stats() {
        return Result.success(payrollService.stats());
    }

    @GetMapping("/{id}")
    @Operation(summary = "发薪单详情（含人员明细）")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("order", payrollService.getOrThrow(id));
        view.put("details", payrollService.details(id));
        return Result.success(view);
    }

    @PostMapping
    @Operation(summary = "创建发薪单（自动汇总金额与人数）")
    public Result<PayrollOrder> create(@RequestBody PayrollCreateRequest request, Authentication authentication) {
        String creator = authentication != null ? authentication.getName() : null;
        return Result.success(payrollService.createOrder(request.getOrder(), request.getDetails(), null, creator));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交发薪单（进入待审批）")
    public Result<PayrollOrder> submit(@PathVariable Long id) {
        return Result.success(payrollService.submit(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过")
    public Result<PayrollOrder> approve(@PathVariable Long id, Authentication authentication) {
        return Result.success(payrollService.approve(id, operator(authentication)));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审批驳回")
    public Result<PayrollOrder> reject(@PathVariable Long id, Authentication authentication) {
        return Result.success(payrollService.reject(id, operator(authentication)));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "撤回发薪单")
    public Result<PayrollOrder> withdraw(@PathVariable Long id) {
        return Result.success(payrollService.withdraw(id));
    }

    private String operator(Authentication authentication) {
        return authentication != null ? authentication.getName() : "system";
    }
}

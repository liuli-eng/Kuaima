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
import com.kuaima.app.domain.boss.repository.BossMerchantAccountRepository;
import com.kuaima.app.domain.payroll.entity.PayrollOrder;
import com.kuaima.app.domain.payroll.model.PayrollCreateRequest;
import com.kuaima.app.domain.payroll.service.PayrollService;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/payrolls")
@Tag(name = "后台-发薪管理", description = "发薪单列表、详情、创建、提交、审批")
public class PayrollController {

    private final PayrollService payrollService;
    private final BossMerchantAccountRepository accountRepository;

    public PayrollController(PayrollService payrollService,
                             BossMerchantAccountRepository accountRepository) {
        this.payrollService = payrollService;
        this.accountRepository = accountRepository;
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
        Long creatorId = currentUserId(authentication);
        // 带明细的薪单审批时必须能定位到归属老板账户，否则会变成无法付款的孤儿单，故在源头拦下
        boolean needsPayment = request.getDetails() != null && !request.getDetails().isEmpty();
        if (needsPayment && (creatorId == null || !accountRepository.existsByBossId(creatorId))) {
            throw new IllegalArgumentException(
                    "后台建单需指定归属老板账号：当前账号下没有可用企业账户，请改由老板端创建发薪单");
        }
        return Result.success(payrollService.createOrder(request.getOrder(), request.getDetails(),
                creatorId, operator(authentication)));
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

    /**
     * 取操作人名称。
     * LoginUser 是 record，UsernamePasswordAuthenticationToken.getName() 在 principal
     * 不是 UserDetails/Principal 时会退化成 principal.toString()，导致库里存进
     * "LoginUser[id=1, username=admin, ...]" 这类脏值，故显式取 username。
     */
    private String operator(Authentication authentication) {
        if (authentication == null) {
            return "system";
        }
        if (authentication.getPrincipal() instanceof LoginUser login && login.username() != null) {
            return login.username();
        }
        return authentication.getName();
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser login) {
            return login.id();
        }
        return null;
    }
}

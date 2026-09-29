package com.kuaima.app.controller.employee;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.employee.service.JoinApplyService;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/join-applies")
@Tag(name = "后台-申请列表", description = "加入申请审批（合并 join_apply 与 enterprise_join_apply，通过后自动建员工）")
public class JoinApplyController {

    /** 请求体 source 取值：小程序扫码/邀请链接产生的申请。 */
    private static final String SOURCE_ENTERPRISE = "enterprise_join_apply";

    private final JoinApplyService joinApplyService;

    public JoinApplyController(JoinApplyService joinApplyService) {
        this.joinApplyService = joinApplyService;
    }

    @GetMapping
    @Operation(summary = "申请列表（status: all/pending/approved/rejected）")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false, defaultValue = "all") String status) {
        return Result.success(joinApplyService.listMerged(status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "通过申请（自动创建员工）")
    public Result<Object> approve(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        String reviewer = operator(authentication);
        if (isEnterpriseApply(body)) {
            return Result.success(joinApplyService.approveEnterpriseApply(id, reviewer));
        }
        return Result.success(joinApplyService.approve(id, reviewer));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝申请")
    public Result<Object> reject(@PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        String reviewer = operator(authentication);
        if (isEnterpriseApply(body)) {
            return Result.success(joinApplyService.rejectEnterpriseApply(id, reviewer));
        }
        return Result.success(joinApplyService.reject(id, reviewer));
    }

    private boolean isEnterpriseApply(Map<String, String> body) {
        return body != null && SOURCE_ENTERPRISE.equals(body.get("source"));
    }

    private String operator(Authentication authentication) {
        if (authentication == null) return "system";
        if (authentication.getPrincipal() instanceof LoginUser login && login.username() != null) {
            return login.username();
        }
        return authentication.getName();
    }
}
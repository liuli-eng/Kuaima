package com.kuaima.app.controller.employee;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.employee.entity.JoinApply;
import com.kuaima.app.domain.employee.service.JoinApplyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/join-applies")
@Tag(name = "后台-申请列表", description = "加入申请审批（通过/拒绝，通过后自动建员工）")
public class JoinApplyController {

    private final JoinApplyService joinApplyService;

    public JoinApplyController(JoinApplyService joinApplyService) {
        this.joinApplyService = joinApplyService;
    }

    @GetMapping
    @Operation(summary = "申请列表（status: all/pending/approved/rejected）")
    public Result<List<JoinApply>> list(@RequestParam(required = false, defaultValue = "all") String status) {
        return Result.success(joinApplyService.list(status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "通过申请（自动创建员工）")
    public Result<JoinApply> approve(@PathVariable Long id, Authentication authentication) {
        return Result.success(joinApplyService.approve(id, operator(authentication)));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝申请")
    public Result<JoinApply> reject(@PathVariable Long id, Authentication authentication) {
        return Result.success(joinApplyService.reject(id, operator(authentication)));
    }

    private String operator(Authentication authentication) {
        return authentication != null ? authentication.getName() : "system";
    }
}

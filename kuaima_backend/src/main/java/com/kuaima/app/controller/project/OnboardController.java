package com.kuaima.app.controller.project;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.project.entity.OnboardApply;
import com.kuaima.app.domain.project.service.OnboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/projects/{projectId}/onboard")
@Tag(name = "老板-入职记录", description = "入职申请列表、统计、通过/拒绝")
public class OnboardController {

    private final OnboardService onboardService;

    public OnboardController(OnboardService onboardService) {
        this.onboardService = onboardService;
    }

    @GetMapping
    @Operation(summary = "入职申请列表（status: all/pending/passed/rejected）")
    public Result<List<OnboardApply>> list(@PathVariable Long projectId,
            @RequestParam(required = false, defaultValue = "all") String status) {
        return Result.success(onboardService.list(projectId, status));
    }

    @GetMapping("/stats")
    @Operation(summary = "入职申请统计：全部/审核中/已通过/已拒绝")
    public Result<Map<String, Long>> stats(@PathVariable Long projectId) {
        return Result.success(onboardService.stats(projectId));
    }

    @PostMapping("/{applyId}/pass")
    @Operation(summary = "通过入职")
    public Result<OnboardApply> pass(@PathVariable Long projectId, @PathVariable Long applyId,
            Authentication authentication) {
        return Result.success(onboardService.pass(applyId, operator(authentication)));
    }

    @PostMapping("/{applyId}/reject")
    @Operation(summary = "拒绝入职")
    public Result<OnboardApply> reject(@PathVariable Long projectId, @PathVariable Long applyId,
            Authentication authentication) {
        return Result.success(onboardService.reject(applyId, operator(authentication)));
    }

    private String operator(Authentication authentication) {
        return authentication != null ? authentication.getName() : "system";
    }
}

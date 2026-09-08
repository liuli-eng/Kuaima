package com.kuaima.app.controller.jobcategory;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.jobcategory.entity.JobCategory;
import com.kuaima.app.domain.jobcategory.entity.JobEnterpriseType;
import com.kuaima.app.domain.jobcategory.model.JobCategoryAdminModels.CreateEnterpriseTypeRequest;
import com.kuaima.app.domain.jobcategory.model.JobCategoryAdminModels.CreateJobRequest;
import com.kuaima.app.domain.jobcategory.service.JobCategoryService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 老板端补充企业类型和工种的接口。 */
@RestController
@RequestMapping("/boss/job-categories")
@Tag(name = "老板-岗位分类", description = "老板补充企业类型和工种")
public class BossJobCategoryController {

    private final JobCategoryService jobCategoryService;

    public BossJobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    @Operation(summary = "新增企业类型", description = "在指定行业下新增企业类型；名称在同一行业内不可重复")
    @PostMapping("/enterprise-types")
    public Result<JobEnterpriseType> createEnterpriseType(
            @RequestBody CreateEnterpriseTypeRequest request, Authentication authentication) {
        requireBoss(authentication);
        if (request == null || request.industryId() == null) {
            throw new IllegalArgumentException("industryId 不能为空");
        }
        return Result.success(jobCategoryService.createEnterpriseType(
                request.industryId(), request.name(), request.sortNo()));
    }

    @Operation(summary = "新增工种", description = "在指定行业的企业类型下新增工种；同一企业类型内名称不可重复")
    @PostMapping("/jobs")
    public Result<JobCategory> createJob(
            @RequestBody CreateJobRequest request, Authentication authentication) {
        requireBoss(authentication);
        if (request == null || request.industryId() == null || request.enterpriseTypeId() == null) {
            throw new IllegalArgumentException("industryId 和 enterpriseTypeId 不能为空");
        }
        return Result.success(jobCategoryService.createJob(
                request.industryId(), request.enterpriseTypeId(), request.name(),
                request.description(), request.sortNo()));
    }

    private void requireBoss(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)
                || !UserRole.BOSS.equals(loginUser.role()) || loginUser.id() == null) {
            throw new IllegalStateException("当前登录账号不是有效的老板账号");
        }
    }
}

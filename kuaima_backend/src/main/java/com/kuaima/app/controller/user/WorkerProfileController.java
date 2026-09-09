package com.kuaima.app.controller.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.model.WorkerProfileModels.UpdateWorkerProfileRequest;
import com.kuaima.app.domain.user.model.WorkerProfileModels.WorkerProfile;
import com.kuaima.app.domain.user.service.WorkerProfileService;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/worker/profile")
@Tag(name = "零工-个人资料", description = "当前登录零工的个人资料查询和修改")
public class WorkerProfileController {

    private final WorkerProfileService workerProfileService;

    public WorkerProfileController(WorkerProfileService workerProfileService) {
        this.workerProfileService = workerProfileService;
    }

    @GetMapping
    @Operation(summary = "获取当前零工个人资料", description = "从JWT获取当前零工ID，不接收前端传入的userId")
    public Result<WorkerProfile> getProfile(Authentication authentication) {
        return Result.success(workerProfileService.getProfile(currentWorkerId(authentication)));
    }

    @PutMapping
    @Operation(summary = "修改当前零工个人资料", description = "手机号为只读字段，修改手机号须通过手机号认证流程")
    public Result<WorkerProfile> updateProfile(@RequestBody UpdateWorkerProfileRequest request,
                                               Authentication authentication) {
        return Result.success(workerProfileService.updateProfile(currentWorkerId(authentication), request));
    }

    private Long currentWorkerId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser
                && loginUser.id() != null && UserRole.USER.equals(loginUser.role())) {
            return loginUser.id();
        }
        throw new ForbiddenBusinessException("当前登录账号不是零工身份");
    }
}

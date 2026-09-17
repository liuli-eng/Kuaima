package com.kuaima.app.controller.user;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.model.WorkerProfileOverview;
import com.kuaima.app.domain.user.service.WorkerProfileOverviewService;
import com.kuaima.app.security.model.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/worker/profile")
@Tag(name = "零工-个人资料")
public class WorkerProfileOverviewController {
    private final WorkerProfileOverviewService service;

    public WorkerProfileOverviewController(WorkerProfileOverviewService service) { this.service = service; }

    @GetMapping("/overview")
    @Operation(summary = "零工个人页聚合数据", description = "身份只从JWT获取；完成率=已完成/全部报名，取消率=取消报名/全部报名，失约率=录用后未到岗/全部录用，早退率=早退工作天数/全部结算工作天数；比例范围0-100，金额单位为分")
    public Result<WorkerProfileOverview> overview(Authentication authentication) {
        return Result.success(service.overview(currentWorkerId(authentication)));
    }

    private Long currentWorkerId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.USER.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是零工身份");
    }
}

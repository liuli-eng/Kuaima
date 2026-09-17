package com.kuaima.app.controller.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.kuaima.app.common.*;
import com.kuaima.app.domain.review.model.BossReviewModels.*;
import com.kuaima.app.domain.review.service.BossReviewService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/worker/items")
public class WorkerBossReviewController {
    private final BossReviewService service;
    public WorkerBossReviewController(BossReviewService service) { this.service = service; }

    @PutMapping("/{itemId}/boss-review")
    @Operation(summary = "创建或修改老板评价", description = "当前JWT零工对本人已完成订单条目幂等创建或修改评价")
    public Result<ReviewView> save(@PathVariable Long itemId, @RequestBody SaveRequest request,
                                   Authentication authentication) {
        return Result.success(service.save(worker(authentication), itemId, request));
    }

    @GetMapping("/{itemId}/boss-review")
    @Operation(summary = "查询订单条目的老板评价", description = "未评价时HTTP 200且data为null")
    public Result<ReviewView> get(@PathVariable Long itemId, Authentication authentication) {
        return Result.success(service.get(worker(authentication), itemId));
    }

    private Long worker(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser u
                && u.id() != null && UserRole.USER.equals(u.role())) return u.id();
        throw new ForbiddenBusinessException("当前登录账号不是零工账号");
    }
}

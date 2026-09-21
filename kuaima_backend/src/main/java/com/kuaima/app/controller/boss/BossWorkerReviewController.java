package com.kuaima.app.controller.boss;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.review.model.WorkerReviewModels.*;
import com.kuaima.app.domain.review.service.WorkerReviewService;
import com.kuaima.app.security.model.LoginUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boss/orders")
@RequiredArgsConstructor
public class BossWorkerReviewController {
    private final WorkerReviewService service;

    @PutMapping("/{orderId}/worker-review")
    public Result<List<ReviewView>> save(@PathVariable Long orderId, @RequestBody SaveRequest request,
                                         Authentication authentication) {
        return Result.success(service.saveOrderReviews(boss(authentication), orderId, request));
    }

    @GetMapping("/{orderId}/worker-review")
    public Result<List<ReviewView>> list(@PathVariable Long orderId, Authentication authentication) {
        return Result.success(service.list(boss(authentication), orderId));
    }

    private Long boss(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user && user.id() != null) return user.id();
        throw new ForbiddenBusinessException("请先登录老板账号");
    }
}

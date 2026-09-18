package com.kuaima.app.controller.boss;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.coupon.service.BossCouponService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boss/coupons")
@Tag(name = "老板端-券包")
public class BossCouponController {
    private final BossCouponService service;
    public BossCouponController(BossCouponService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "当前老板券包", description = "身份从JWT获取；status支持UNUSED、USED、EXPIRED")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String status,
                                                   Authentication authentication) {
        return Result.success(service.list(currentBossId(authentication), status));
    }

    @GetMapping("/{userCouponId}/available-orders")
    @Operation(summary = "优惠券可用待结算订单", description = "按当前老板 JWT 查询该优惠券可核销的待结算订单")
    public Result<Map<String, Object>> availableOrders(@PathVariable Long userCouponId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        Authentication authentication) {
        Map<String, Object> data = service.availableOrders(currentBossId(authentication), userCouponId, page, size);
        return Result.success(data, (Integer) data.get("page"), ((Number) data.get("total")).longValue());
    }

    private Long currentBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.BOSS.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是老板身份");
    }
}

package com.kuaima.app.controller.finance;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.coupon.entity.UserCoupon;
import com.kuaima.app.domain.coupon.service.BossCouponService;
import com.kuaima.app.domain.coupon.service.CouponClaimService;

/**
 * 优惠券。
 */
@RestController
@RequestMapping("/coupons")
@Tag(name = "优惠券", description = "用户优惠券管理")
public class CouponController {

    private final BossCouponService couponService;
    private final CouponClaimService claimService;

    public CouponController(BossCouponService couponService, CouponClaimService claimService) {
        this.couponService = couponService;
        this.claimService = claimService;
    }

    /** 优惠券列表：GET /coupons?userId=1&status=UNUSED */
    @Operation(summary = "优惠券列表", description = "按用户 id 查询领取记录及完整券面信息；status 支持 UNUSED、USED、EXPIRED")
    @GetMapping
    public Result<List<Map<String, Object>>> listCoupons(@RequestParam Long userId,
                                                          @RequestParam(required = false) String status) {
        return Result.success(couponService.list(userId, status));
    }

    /** 领取优惠券：POST /coupons/{id}/claim?userId=1 */
    @Operation(summary = "领取优惠券", description = "为用户领取指定优惠券，status=UNUSED；优惠券定义含 validDays 时按当前时间+有效天数计算过期时间；优惠券不存在抛出 EntityNotFoundException")
    @PostMapping("/{id}/claim")
    public Result<UserCoupon> claimCoupon(@PathVariable Long id, @RequestParam Long userId) {
        return Result.success(claimService.claim(id, userId));
    }
}

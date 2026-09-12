package com.kuaima.app.controller.finance;

import java.sql.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.coupon.entity.Coupon;
import com.kuaima.app.domain.coupon.entity.UserCoupon;
import com.kuaima.app.domain.coupon.repository.CouponRepository;
import com.kuaima.app.domain.coupon.repository.UserCouponRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 优惠券。
 */
@RestController
@RequestMapping("/coupons")
@Tag(name = "优惠券", description = "用户优惠券管理")
public class CouponController {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    public CouponController(CouponRepository couponRepository, UserCouponRepository userCouponRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
    }

    /** 优惠券列表：GET /coupons?userId=1&status=UNUSED */
    @Operation(summary = "优惠券列表", description = "按用户 id 查询优惠券；status 可选（如 UNUSED/USED），传入时按状态过滤")
    @GetMapping
    public Result<List<UserCoupon>> listCoupons(@RequestParam Long userId,
                                                @RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return Result.success(userCouponRepository.findByUserIdAndStatus(userId, status));
        }
        return Result.success(userCouponRepository.findByUserId(userId));
    }

    /** 领取优惠券：POST /coupons/{id}/claim?userId=1 */
    @Operation(summary = "领取优惠券", description = "为用户领取指定优惠券，status=UNUSED；优惠券定义含 validDays 时按当前时间+有效天数计算过期时间；优惠券不存在抛出 EntityNotFoundException")
    @PostMapping("/{id}/claim")
    @Transactional
    public Result<UserCoupon> claimCoupon(@PathVariable Long id, @RequestParam Long userId) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("优惠券不存在: " + id));
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(id);
        uc.setStatus("UNUSED");
        if (coupon.getValidDays() != null) {
            uc.setExpireAt(new Date(System.currentTimeMillis() + coupon.getValidDays() * 24L * 60 * 60 * 1000));
        }
        return Result.success(userCouponRepository.save(uc));
    }
}

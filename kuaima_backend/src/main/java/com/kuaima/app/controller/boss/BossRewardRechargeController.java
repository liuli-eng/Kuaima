package com.kuaima.app.controller.boss;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.reward.model.WorkerRewardModels.RechargeRequest;
import com.kuaima.app.domain.reward.service.RewardRechargeService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/rewards")
@Tag(name = "老板端-奖励金充值")
public class BossRewardRechargeController {
    private final RewardRechargeService service;

    public BossRewardRechargeController(RewardRechargeService service) {
        this.service = service;
    }

    @PostMapping("/recharge")
    @Operation(summary = "创建奖励金微信充值订单", description = "amount单位为元；支付成功回调后才入奖励金账户")
    public Result<Map<String, Object>> recharge(@RequestBody RechargeRequest body,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            Authentication authentication) {
        if (body == null) throw new IllegalArgumentException("请求体不能为空");
        return Result.success(service.create(bossId(authentication), enterpriseId(authentication),
                body.amount(), body.payMethod(), key));
    }

    @GetMapping("/recharge/config")
    @Operation(summary = "奖励金充值配置")
    public Result<Map<String, Object>> rechargeConfig(Authentication authentication) {
        return Result.success(service.config(bossId(authentication)));
    }

    @GetMapping("/recharge/{orderNo}")
    @Operation(summary = "查询奖励金充值订单")
    public Result<Map<String, Object>> rechargeDetail(@PathVariable String orderNo, Authentication authentication) {
        return Result.success(service.detail(bossId(authentication), orderNo));
    }

    @GetMapping("/recharge-records")
    @Operation(summary = "奖励金充值记录")
    public Result<Map<String, Object>> rechargeRecords(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "ALL") String status,
            Authentication authentication) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page 或 size 参数无效");
        return Result.success(service.records(bossId(authentication), page, size, status));
    }

    private Long bossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.BOSS.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }

    private Long enterpriseId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user) return user.enterpriseId();
        return null;
    }
}

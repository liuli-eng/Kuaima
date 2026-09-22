package com.kuaima.app.controller.worker;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.wallet.service.WorkerWalletWechatWithdrawalService;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/worker/wallet")
@Tag(name = "零工-钱包")
public class WorkerWalletController {
    private final WorkerWalletWechatWithdrawalService service;

    public WorkerWalletController(WorkerWalletWechatWithdrawalService service) { this.service = service; }

    @PostMapping("/withdraw")
    @Operation(summary = "钱包提现到微信零钱", description = "amount单位为元；身份只取JWT；必须提供Idempotency-Key")
    public Result<Map<String, Object>> withdraw(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            Authentication authentication) {
        Object raw = body == null ? null : body.get("amount");
        BigDecimal amount;
        try { amount = raw == null ? null : new BigDecimal(String.valueOf(raw)); }
        catch (Exception e) { throw new IllegalArgumentException("amount 必须是元金额"); }
        Result<Map<String, Object>> result = Result.success(service.withdraw(workerId(authentication), amount, key));
        result.setMessage("微信提现申请已提交"); return result;
    }

    private Long workerId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.USER.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是零工身份");
    }
}

package com.kuaima.app.controller.worker;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.reward.model.WorkerRewardModels.WithdrawalRequest;
import com.kuaima.app.domain.reward.service.WorkerRewardService;
import com.kuaima.app.domain.reward.service.WorkerRewardWithdrawalFailedException;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/worker/rewards")
@Tag(name = "零工-奖励金")
public class WorkerRewardController {
    private final WorkerRewardService service;

    public WorkerRewardController(WorkerRewardService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    @Operation(summary = "奖励金账户概览", description = "身份从JWT获取；balance、withdrawableBalance、minimumWithdrawAmount 单位均为分")
    public Result<java.util.Map<String, Object>> overview(Authentication authentication) {
        return Result.success(service.overview(workerId(authentication)));
    }

    @GetMapping("/records")
    @Operation(summary = "奖励金明细分页", description = "type=ALL/INCOME/WITHDRAW；收入为正数，提现为负数，金额单位为分")
    public Result<java.util.Map<String, Object>> records(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String type,
            Authentication authentication) {
        validatePage(page, size);
        return Result.success(service.records(workerId(authentication), type,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"))));
    }

    @GetMapping("/rules")
    @Operation(summary = "奖励金获取规则", description = "规则、奖励金额和提现配置来自数据库配置")
    public Result<java.util.Map<String, Object>> rules(Authentication authentication) {
        workerId(authentication);
        return Result.success(service.rules());
    }

    @PostMapping("/withdraw")
    @Operation(summary = "申请奖励金提现", description = "amount单位为分；必须携带唯一Idempotency-Key，身份从JWT获取")
    public Result<java.util.Map<String, Object>> withdraw(
            @RequestBody WithdrawalRequest body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication authentication) {
        try {
            Result<java.util.Map<String, Object>> result =
                    Result.success(service.withdraw(workerId(authentication), body == null ? null : body.amount(),
                            body == null ? null : body.channel(), idempotencyKey));
            result.setMessage("提现申请已提交");
            return result;
        } catch (WorkerRewardWithdrawalFailedException e) {
            return Result.error(502, e.getMessage());
        }
    }

    private Long workerId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.USER.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是零工身份");
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 必须大于等于0，size范围为1-100");
        }
    }
}

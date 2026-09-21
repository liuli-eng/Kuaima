package com.kuaima.app.controller.worker;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.points.entity.PointsWithdrawal;
import com.kuaima.app.domain.points.model.PointsWithdrawalModels.WithdrawalView;
import com.kuaima.app.domain.points.model.PointsWithdrawalModels.ApplyRequest;
import com.kuaima.app.domain.points.model.PointsWithdrawalModels.PageView;
import com.kuaima.app.domain.points.service.WorkerPointsWithdrawalService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/worker/points")
@Tag(name = "零工-积分提现", description = "零工积分提现规则、申请和记录")
public class WorkerPointsWithdrawalController {
    private final WorkerPointsWithdrawalService service;
    public WorkerPointsWithdrawalController(WorkerPointsWithdrawalService service) { this.service = service; }

    @GetMapping("/withdraw-config")
    @Operation(summary = "查询积分提现规则和账户摘要")
    public Result<Map<String, Object>> config(Authentication authentication) { return Result.success(service.config(worker(authentication))); }

    @PostMapping("/withdrawals")
    @Operation(summary = "提交积分提现申请")
    public Result<WithdrawalView> apply(@RequestBody ApplyRequest body,
                                           @RequestHeader(value = "Idempotency-Key", required = false) String key,
                                           Authentication authentication) {
        Integer value = body == null ? null : body.points();
        String channel = body == null || body.channel() == null ? null : body.channel().trim().toUpperCase();
        return Result.success(service.view(service.apply(worker(authentication), value, channel, key)));
    }

    @GetMapping("/withdrawals")
    @Operation(summary = "查询积分提现记录")
    public Result<PageView<WithdrawalView>> list(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(defaultValue = "ALL") String status,
                                               Authentication authentication) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page 或 size 参数无效");
        var p = service.list(worker(authentication), status == null ? "ALL" : status.trim().toUpperCase(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt")));
        return Result.success(new PageView<>(p.getContent().stream().map(service::view).toList(), page, size, p.getTotalElements()));
    }

    @GetMapping("/withdrawals/{id}")
    @Operation(summary = "查询积分提现详情")
    public Result<WithdrawalView> detail(@PathVariable Long id, Authentication authentication) { return Result.success(service.view(service.detail(worker(authentication), id))); }

    private Long worker(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.USER.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("仅零工身份可以操作积分提现");
    }
}

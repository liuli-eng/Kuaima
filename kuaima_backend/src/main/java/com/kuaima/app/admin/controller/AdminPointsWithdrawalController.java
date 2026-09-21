package com.kuaima.app.admin.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.points.entity.PointsWithdrawal;
import com.kuaima.app.domain.points.model.PointsWithdrawalModels.WithdrawalView;
import com.kuaima.app.domain.points.model.PointsWithdrawalModels.PageView;
import com.kuaima.app.domain.points.model.PointsWithdrawalModels.RejectRequest;
import com.kuaima.app.domain.points.service.WorkerPointsWithdrawalService;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/points/withdrawals")
@Tag(name = "后台-积分提现", description = "积分提现审核和模拟出款")
public class AdminPointsWithdrawalController {
    private final WorkerPointsWithdrawalService service;
    public AdminPointsWithdrawalController(WorkerPointsWithdrawalService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "积分提现申请列表")
    public Result<PageView<WithdrawalView>> list(@RequestParam(defaultValue = "ALL") String status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               Authentication authentication) {
        LoginUser admin = admin(authentication, false);
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page 或 size 参数无效");
        var p = service.adminList(status == null ? "ALL" : status.trim().toUpperCase(), PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "appliedAt")));
        return Result.success(new PageView<>(p.getContent().stream().map(service::view).toList(), page, size, p.getTotalElements()));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审核通过并模拟出款")
    public Result<WithdrawalView> approve(@PathVariable Long id, Authentication authentication) { LoginUser admin = admin(authentication, true); return Result.success(service.view(service.approve(id, admin.id()))); }

    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回提现并退回积分")
    public Result<WithdrawalView> reject(@PathVariable Long id, @RequestBody(required = false) RejectRequest body, Authentication authentication) { LoginUser admin = admin(authentication, true); return Result.success(service.view(service.reject(id, admin.id(), body == null ? null : body.reason()))); }

    @PostMapping("/{id}/retry")
    @Operation(summary = "重新处理失败提现")
    public Result<WithdrawalView> retry(@PathVariable Long id, Authentication authentication) { LoginUser admin = admin(authentication, true); return Result.success(service.view(service.retry(id, admin.id()))); }

    private LoginUser admin(Authentication authentication, boolean write) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)
                || user.id() == null || user.role() == null || !user.role().startsWith("ADMIN_")) throw new ForbiddenBusinessException("仅管理员可操作");
        if (write && "ADMIN_VIEWER".equals(user.role())) throw new ForbiddenBusinessException("当前管理员无积分提现管理权限");
        return user;
    }
}

package com.kuaima.app.admin.controller;

import com.kuaima.app.admin.entity.ScheduledJobExecution;
import com.kuaima.app.admin.repository.ScheduledJobExecutionRepository;
import com.kuaima.app.admin.scheduler.CouponDistributionScheduler;
import com.kuaima.app.admin.scheduler.RewardCampaignScheduler;
import com.kuaima.app.admin.scheduler.ScheduledMessageTask;
import com.kuaima.app.admin.scheduler.CreditScoreScheduler;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.security.model.LoginUser;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 定时任务执行记录查询。任务执行仍由后端代码和配置管理。 */
@RestController
@RequestMapping("/admin/scheduled-jobs")
public class AdminScheduledJobController {
    private final ScheduledJobExecutionRepository executions;
    private final CouponDistributionScheduler coupons;
    private final RewardCampaignScheduler rewards;
    private final ScheduledMessageTask messages;
    private final CreditScoreScheduler creditScores;

    public AdminScheduledJobController(ScheduledJobExecutionRepository executions,
                                       CouponDistributionScheduler coupons,
                                       RewardCampaignScheduler rewards,
                                       ScheduledMessageTask messages,
                                       CreditScoreScheduler creditScores) {
        this.executions = executions; this.coupons = coupons; this.rewards = rewards;
        this.messages = messages; this.creditScores = creditScores;
    }

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String jobCode,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Authentication authentication) {
        admin(authentication, false);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(100, Math.max(1, size));
        var result = jobCode == null || jobCode.isBlank()
                ? executions.findAllByOrderByStartedAtDesc(PageRequest.of(safePage, safeSize))
                : executions.findByJobCodeOrderByStartedAtDesc(jobCode, PageRequest.of(safePage, safeSize));
        return Result.success(Map.of("content", result.getContent(), "total", result.getTotalElements(),
                "page", result.getNumber(), "size", result.getSize()));
    }

    @PostMapping("/{jobCode}/run")
    public Result<?> run(@org.springframework.web.bind.annotation.PathVariable String jobCode,
                         Authentication authentication) {
        LoginUser user = admin(authentication, true);
        switch (jobCode) {
            case "coupon-distribution" -> coupons.distributeDue("MANUAL");
            case "reward-campaign" -> rewards.executeDue("MANUAL");
            case "scheduled-message" -> messages.executeScheduledSend("MANUAL");
            case "credit-score-daily" -> creditScores.daily("MANUAL");
            case "credit-score-monthly" -> creditScores.monthly("MANUAL");
            default -> throw new IllegalArgumentException("不支持手动执行的任务: " + jobCode);
        }
        return Result.success(Map.of("jobCode", jobCode, "triggerType", "MANUAL", "operatorId", user.id()));
    }

    private LoginUser admin(Authentication authentication, boolean write) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)
                || user.role() == null || !user.role().startsWith("ADMIN_")) {
            throw new ForbiddenBusinessException("仅管理员可查看定时任务");
        }
        if (write && user.role().endsWith("VIEWER")) throw new ForbiddenBusinessException("当前管理员无任务执行权限");
        return user;
    }
}

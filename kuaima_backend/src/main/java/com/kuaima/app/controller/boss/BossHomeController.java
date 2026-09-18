package com.kuaima.app.controller.boss;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.model.BossHomeModels.Overview;
import com.kuaima.app.domain.boss.model.BossHomeModels.Schedule;
import com.kuaima.app.domain.boss.service.BossHomeService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.enterprise.service.EnterpriseContextService;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/home")
@Tag(name = "老板-首页", description = "老板首页真实数据聚合")
public class BossHomeController {
    private final BossHomeService homeService;
    private final EnterpriseContextService enterpriseContexts;

    public BossHomeController(BossHomeService homeService) {
        this(homeService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public BossHomeController(BossHomeService homeService, EnterpriseContextService enterpriseContexts) {
        this.homeService = homeService;
        this.enterpriseContexts = enterpriseContexts;
    }

    @GetMapping("/overview")
    @Operation(summary = "首页概览", description = "老板ID取自JWT；返回城市、附近零工、当前招聘账号和昨天至后天需求人数。longitude/latitude预留给未来真实距离查询，当前附近零工按城市统计")
    public Result<Overview> overview(@RequestParam(required = false) Double longitude,
                                     @RequestParam(required = false) Double latitude,
                                     @RequestParam(required = false) String city,
                                     @RequestParam(required = false) Long accountId,
                                     Authentication authentication) {
        LoginUser operator = requireBoss(authentication);
        if (operator.enterpriseId() != null && enterpriseContexts != null) {
            enterpriseContexts.require(authentication, "ORDER_VIEW");
            return Result.success(homeService.overviewByEnterprise(operator.enterpriseId(), operator.id(), city, accountId));
        }
        return Result.success(homeService.overview(
                operator.id(), operator.accountGroupOwnerId(), city, accountId));
    }

    @GetMapping("/schedule")
    @Operation(summary = "首页排班详情", description = "按服务器时区和订单startTime日期，批量聚合当前老板的需求、报名状态、结算和订单记录；accountId仅校验当前老板账号归属")
    public Result<Schedule> schedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long accountId,
            Authentication authentication) {
        LoginUser operator = requireBoss(authentication);
        if (operator.enterpriseId() != null && enterpriseContexts != null) {
            enterpriseContexts.require(authentication, "ORDER_VIEW");
            return Result.success(homeService.scheduleByEnterprise(operator.enterpriseId(), operator.id(), date, accountId));
        }
        return Result.success(homeService.schedule(
                operator.id(), operator.accountGroupOwnerId(), date, accountId));
    }

    private Long requireBossId(Authentication authentication) {
        return requireBoss(authentication).id();
    }

    private LoginUser requireBoss(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.BOSS.equals(user.role())) return user;
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }
}

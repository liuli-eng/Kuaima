package com.kuaima.app.controller.wallet;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.wallet.model.PendingSettlementModels.PendingSettlementOrder;
import com.kuaima.app.domain.wallet.service.SettlementService;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 老板端结算聚合接口。 */
@RestController
@RequestMapping("/boss/settlements")
@Tag(name = "老板-结算", description = "老板待结算岗位聚合查询")
public class BossSettlementController {

    private final SettlementService settlementService;

    public BossSettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Operation(summary = "老板全部待结算订单", description = "按当前老板 JWT 返回全部岗位的待结算报名记录、零工、金额和结算状态；不接受前端传入 userId/bossId")
    @GetMapping("/pending")
    public Result<List<PendingSettlementOrder>> listPending(Authentication authentication) {
        return Result.success(settlementService.listPendingByBoss(requireBossId(authentication)));
    }

    private Long requireBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser
                && UserRole.BOSS.equals(loginUser.role()) && loginUser.id() != null) {
            return loginUser.id();
        }
        throw new IllegalStateException("当前登录账号不是有效的老板账号");
    }
}

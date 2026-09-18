package com.kuaima.app.controller.boss;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.model.BossRecruitSettingsModels.Settings;
import com.kuaima.app.domain.boss.service.BossRecruitSettingsService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;
import com.kuaima.app.domain.enterprise.service.EnterpriseContextService;

@RestController
@RequestMapping("/boss/recruit-settings")
public class BossRecruitSettingsController {
    private final BossRecruitSettingsService service;
    private final EnterpriseContextService contexts;
    public BossRecruitSettingsController(BossRecruitSettingsService service) { this(service, null); }
    @org.springframework.beans.factory.annotation.Autowired public BossRecruitSettingsController(BossRecruitSettingsService service, EnterpriseContextService contexts) { this.service = service; this.contexts = contexts; }
    @GetMapping
    public Result<Settings> get(Authentication authentication) { var c = contexts == null ? null : contexts.require(authentication, "SETTINGS_VIEW"); return Result.success(contexts == null ? service.get(requireBoss(authentication)) : service.getByEnterprise(c.enterprise().getId(), c.user().getPhone())); }
    @PutMapping
    public Result<Settings> save(@RequestBody Settings settings, Authentication authentication) { var c = contexts == null ? null : contexts.require(authentication, "SETTINGS_WRITE"); return Result.success(contexts == null ? service.save(requireBoss(authentication), settings) : service.saveByEnterprise(c.enterprise().getId(), c.user().getId(), settings)); }
    private Long requireBoss(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && UserRole.BOSS.equals(user.role()) && user.id() != null) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }
}

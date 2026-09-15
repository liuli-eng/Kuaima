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

@RestController
@RequestMapping("/boss/recruit-settings")
public class BossRecruitSettingsController {
    private final BossRecruitSettingsService service;
    public BossRecruitSettingsController(BossRecruitSettingsService service) { this.service = service; }
    @GetMapping
    public Result<Settings> get(Authentication authentication) { return Result.success(service.get(requireBoss(authentication))); }
    @PutMapping
    public Result<Settings> save(@RequestBody Settings settings, Authentication authentication) { return Result.success(service.save(requireBoss(authentication), settings)); }
    private Long requireBoss(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && UserRole.BOSS.equals(user.role()) && user.id() != null) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }
}

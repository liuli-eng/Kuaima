package com.kuaima.app.controller.boss;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossRecruitAccount;
import com.kuaima.app.domain.boss.model.BossProfileModels.AccountView;
import com.kuaima.app.domain.boss.model.BossProfileModels.RecruitAccounts;
import com.kuaima.app.domain.boss.repository.BossRecruitAccountRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/recruit-accounts")
@Tag(name = "老板-招聘账号", description = "当前老板的招聘账号查询与切换")
public class BossRecruitAccountController {
    private final BossRecruitAccountRepository repository;
    public BossRecruitAccountController(BossRecruitAccountRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Transactional
    @Operation(summary = "招聘账号列表", description = "仅返回当前JWT老板的账号并保证唯一current；开工码和早退码只返回真实配置，不生成伪造值")
    public Result<RecruitAccounts> list(Authentication authentication) {
        Long bossId = requireBossId(authentication);
        List<BossRecruitAccount> accounts = repository.findByOwnerUserIdOrderByIdAsc(bossId);
        if (accounts.isEmpty()) {
            return Result.success(new RecruitAccounts(false, 0, null, List.of()));
        }
        BossRecruitAccount current = normalizeCurrent(accounts);
        List<AccountView> views = accounts.stream().map(this::toView).toList();
        return Result.success(new RecruitAccounts(true, views.size(), current.getId(), views));
    }

    @PutMapping("/current")
    @Transactional
    @Operation(summary = "切换当前招聘账号", description = "accountId必须属于当前JWT老板；切换后数据库中仅保留一个current账号")
    public Result<BossRecruitAccount> current(@RequestBody Map<String, Long> body, Authentication authentication) {
        Long bossId = requireBossId(authentication);
        Long accountId = body.get("accountId");
        if (accountId == null) throw new IllegalArgumentException("accountId不能为空");
        BossRecruitAccount selected = repository.findByIdAndOwnerUserId(accountId, bossId)
                .orElseThrow(() -> new ForbiddenBusinessException("账号不属于当前老板"));
        List<BossRecruitAccount> accounts = repository.findByOwnerUserIdOrderByIdAsc(bossId);
        accounts.forEach(account -> account.setCurrent(account.getId().equals(accountId)));
        repository.saveAll(accounts);
        selected.setCurrent(true);
        return Result.success(selected);
    }

    private BossRecruitAccount normalizeCurrent(List<BossRecruitAccount> accounts) {
        BossRecruitAccount selected = accounts.stream().filter(a -> Boolean.TRUE.equals(a.getCurrent()))
                .findFirst().orElse(accounts.get(0));
        accounts.forEach(account -> account.setCurrent(account.getId().equals(selected.getId())));
        repository.saveAll(accounts);
        return selected;
    }

    private AccountView toView(BossRecruitAccount account) {
        return new AccountView(account.getId(), text(account.getName()), account.getAvatar(),
                text(account.getAuthorizationType()), account.getWorkCode(), account.getLeaveCode(),
                Boolean.TRUE.equals(account.getCurrent()));
    }

    private String text(String value) { return value == null ? "" : value; }

    private Long requireBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.BOSS.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }
}

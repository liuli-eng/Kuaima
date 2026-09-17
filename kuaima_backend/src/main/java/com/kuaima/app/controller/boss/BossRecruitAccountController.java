package com.kuaima.app.controller.boss;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.interceptor.AdminLogInterceptor;
import com.kuaima.app.admin.service.AdminLogService;
import com.kuaima.app.common.BusinessHttpException;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossRecruitAccount;
import com.kuaima.app.domain.boss.model.BossProfileModels.AccountView;
import com.kuaima.app.domain.boss.model.BossProfileModels.AddAccountRequest;
import com.kuaima.app.domain.boss.model.BossProfileModels.AddAccountResponse;
import com.kuaima.app.domain.boss.model.BossProfileModels.RecruitAccounts;
import com.kuaima.app.domain.boss.repository.BossRecruitAccountRepository;
import com.kuaima.app.domain.boss.service.BossRecruitAccountService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/boss/recruit-accounts")
@Tag(name = "老板-招聘账号", description = "当前老板的招聘账号查询、添加与切换")
public class BossRecruitAccountController {
    private static final long ADD_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(3);
    private static final Pattern MAINLAND_PHONE_PATTERN = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");

    private final BossRecruitAccountRepository repository;
    private final UserRepository userRepository;
    private final BossRecruitAccountService accountService;
    private final AdminLogService logService;
    private final ConcurrentHashMap<Long, Long> lastAddRequests = new ConcurrentHashMap<>();

    public BossRecruitAccountController(BossRecruitAccountRepository repository,
                                        UserRepository userRepository,
                                        BossRecruitAccountService accountService,
                                        AdminLogService logService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.logService = logService;
    }

    @GetMapping
    @Transactional
    @Operation(summary = "招聘账号列表", description = "仅返回当前JWT老板的账号并保证唯一current")
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

    @PostMapping
    @Operation(summary = "添加新登录账号", description = "消费短信验证码并绑定已有老板账号，或创建新的老板账号")
    public Result<AddAccountResponse> add(@RequestBody AddAccountRequest body,
                                          Authentication authentication,
                                          HttpServletRequest request) {
        LoginUser operator = requireBoss(authentication);
        String maskedPhone = BossRecruitAccountService.maskPhone(body == null ? null : body.phone());
        String ip = AdminLogInterceptor.clientIp(request);
        try {
            enforceAddRateLimit(operator.id());
            AddAccountResponse response = accountService.add(operator.id(), body);
            logService.record(maskLoggingIdentifier(operator.username()), operator.id(), "添加招聘账号",
                    "账号=" + response.account().id(), ip, "成功", "手机号=" + maskedPhone);
            Result<AddAccountResponse> result = Result.success(response);
            result.setMessage("账号添加成功");
            return result;
        } catch (RuntimeException e) {
            logService.record(maskLoggingIdentifier(operator.username()), operator.id(), "添加招聘账号",
                    "手机号=" + maskedPhone, ip, "失败", safeReason(e));
            if (isClientFacingError(e)) throw e;
            throw new BusinessHttpException(HttpStatus.INTERNAL_SERVER_ERROR, "添加账号失败");
        }
    }

    @PutMapping("/current")
    @Transactional
    @Operation(summary = "切换当前招聘账号", description = "accountId必须属于当前JWT老板；切换后数据库中仅保留一个current账号")
    public Result<AccountView> current(@RequestBody Map<String, ?> body,
                                       Authentication authentication,
                                       HttpServletRequest request) {
        LoginUser operator = requireBoss(authentication);
        Long accountId = accountId(body);
        String ip = AdminLogInterceptor.clientIp(request);
        try {
            if (accountId == null) throw new IllegalArgumentException("accountId不能为空");
            BossRecruitAccount selected = repository.findByIdAndOwnerUserId(accountId, operator.id())
                    .orElseThrow(() -> new ForbiddenBusinessException("账号不属于当前老板"));
            List<BossRecruitAccount> accounts = repository.findByOwnerUserIdOrderByIdAsc(operator.id());
            accounts.forEach(account -> account.setCurrent(account.getId().equals(accountId)));
            repository.saveAll(accounts);
            selected.setCurrent(true);
            AccountView view = toView(selected);
            logService.record(maskLoggingIdentifier(operator.username()), operator.id(), "切换招聘账号",
                    "账号=" + accountId, ip, "成功", null);
            return Result.success(view);
        } catch (RuntimeException e) {
            logService.record(maskLoggingIdentifier(operator.username()), operator.id(), "切换招聘账号",
                    "账号=" + accountId, ip, "失败", safeReason(e));
            throw e;
        }
    }

    private void enforceAddRateLimit(Long bossId) {
        long now = System.currentTimeMillis();
        Long previous = lastAddRequests.put(bossId, now);
        if (previous != null && now - previous < ADD_INTERVAL_MILLIS) {
            throw new BusinessHttpException(HttpStatus.TOO_MANY_REQUESTS, "操作过于频繁，请稍后重试");
        }
    }

    /** Jackson 会把小数字 JSON 反序列化为 Integer，泛型擦除后不能直接按 Long 强转。 */
    private Long accountId(Map<String, ?> body) {
        Object value = body == null ? null : body.get("accountId");
        return value instanceof Number number ? Long.valueOf(number.longValue()) : null;
    }

    private BossRecruitAccount normalizeCurrent(List<BossRecruitAccount> accounts) {
        BossRecruitAccount selected = accounts.stream().filter(a -> Boolean.TRUE.equals(a.getCurrent()))
                .findFirst().orElse(accounts.get(0));
        accounts.forEach(account -> account.setCurrent(account.getId().equals(selected.getId())));
        repository.saveAll(accounts);
        return selected;
    }

    private AccountView toView(BossRecruitAccount account) {
        String phone = userRepository.findById(account.getTargetUserId()).map(user -> user.getPhone())
                .map(BossRecruitAccountService::maskPhone).orElse("");
        return new AccountView(account.getId(), text(account.getName()), account.getAvatar(), phone,
                text(account.getAuthorizationType()), account.getWorkCode(), account.getLeaveCode(),
                Boolean.TRUE.equals(account.getCurrent()));
    }

    private String safeReason(RuntimeException e) {
        return isClientFacingError(e) ? e.getMessage() : "服务器内部错误";
    }

    private boolean isClientFacingError(RuntimeException e) {
        return e instanceof BusinessHttpException || e instanceof ForbiddenBusinessException
                || e instanceof IllegalArgumentException || e instanceof EntityNotFoundException;
    }

    /** 审计日志不能携带完整手机号；兼容历史“手机号作为用户名”的账号。 */
    private String maskLoggingIdentifier(String username) {
        if (username == null) return "system";
        Matcher matcher = MAINLAND_PHONE_PATTERN.matcher(username);
        return matcher.replaceAll(match -> match.group().substring(0, 3) + "****"
                + match.group().substring(7));
    }

    private String text(String value) {
        return value == null ? "" : value;
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

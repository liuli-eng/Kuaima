package com.kuaima.app.controller.finance;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.subaccount.entity.SubAccount;
import com.kuaima.app.domain.subaccount.repository.SubAccountRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/sub-accounts")
@Tag(name = "老板-授权员工", description = "基于当前JWT老板的数据隔离、创建、删除和角色维护")
public class SubAccountController {
    private static final Set<String> ROLES = Set.of("ADMIN", "FINANCE", "OPERATOR");

    private final SubAccountRepository subAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String devVerificationCode;

    public SubAccountController(SubAccountRepository subAccountRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                Environment environment,
                                @Value("${aliyun.sms.mock-code:}") String devVerificationCode) {
        this.subAccountRepository = subAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.devVerificationCode = devVerificationCode;
    }

    @GetMapping
    @Operation(summary = "授权员工列表", description = "parentId仅兼容旧调用，实际只查询当前JWT老板；传入其他老板ID返回403")
    public Result<List<SubAccount>> listSubAccounts(@RequestParam(required = false) Long parentId,
                                                     Authentication authentication) {
        Long bossId = requireBossId(authentication);
        rejectOtherParent(parentId, bossId);
        List<SubAccount> accounts = subAccountRepository.findByParentId(bossId);
        Map<Long, User> users = userRepository.findAllById(accounts.stream().map(SubAccount::getUserId).toList())
                .stream().collect(java.util.stream.Collectors.toMap(User::getId, u -> u));
        accounts.forEach(account -> account.setPhone(users.containsKey(account.getUserId())
                ? users.get(account.getUserId()).getPhone() : null));
        return Result.success(accounts);
    }

    @PostMapping
    @Transactional
    @Operation(summary = "开通授权员工", description = "请求phone、code、role；老板ID只取JWT。验证码仅在dev profile且启用aliyun.sms.mock-enabled时使用开发验证码，生产环境未接短信服务时拒绝创建")
    public Result<SubAccount> createSubAccount(@RequestBody Map<String, Object> body,
                                               Authentication authentication) {
        Long bossId = requireBossId(authentication);
        rejectOtherParent(toLong(body.get("parentId")), bossId);
        String phone = text(body.get("phone"));
        String code = text(body.get("code"));
        String role = normalizeRole(text(body.get("role")));
        if (!phone.matches("^1\\d{10}$")) throw new IllegalArgumentException("手机号格式不正确");
        verifyCode(code);

        User user = resolveOrCreateUser(bossId, phone, role, body);
        if (subAccountRepository.existsByParentIdAndUserId(bossId, user.getId())) {
            throw new IllegalStateException("该员工已授权");
        }
        SubAccount sub = new SubAccount();
        sub.setParentId(bossId);
        sub.setUserId(user.getId());
        sub.setPhone(phone);
        sub.setRole(role);
        sub.setStatus("ACTIVE");
        return Result.success(subAccountRepository.save(sub));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "删除授权员工", description = "仅允许当前JWT老板删除自己的子账号；越权返回403")
    public Result<Void> deleteSubAccount(@PathVariable Long id, Authentication authentication) {
        Long bossId = requireBossId(authentication);
        SubAccount sub = owned(id, bossId);
        userRepository.findById(sub.getUserId()).ifPresent(user -> {
            user.setStatus("冻结");
            userRepository.save(user);
        });
        subAccountRepository.delete(sub);
        return Result.success();
    }

    @PutMapping("/{id}/role")
    @Transactional
    @Operation(summary = "修改授权员工角色", description = "仅允许当前JWT老板修改自己的子账号；role支持ADMIN、FINANCE、OPERATOR")
    public Result<SubAccount> updateRole(@PathVariable Long id, @RequestParam String role,
                                         Authentication authentication) {
        Long bossId = requireBossId(authentication);
        SubAccount sub = owned(id, bossId);
        String normalizedRole = normalizeRole(role);
        sub.setRole(normalizedRole);
        userRepository.findById(sub.getUserId()).ifPresent(user -> {
            user.setSubRole(normalizedRole);
            userRepository.save(user);
        });
        return Result.success(subAccountRepository.save(sub));
    }

    private User resolveOrCreateUser(Long bossId, String phone, String role, Map<String, Object> body) {
        List<User> matches = userRepository.findByPhone(phone);
        if (!matches.isEmpty()) {
            User user = matches.stream().filter(u -> bossId.equals(u.getParentUserId())).findFirst()
                    .orElseThrow(() -> new ForbiddenBusinessException("该手机号已属于其他账号，不能直接授权"));
            user.setSubRole(role);
            user.setStatus("正常");
            return userRepository.save(user);
        }
        User user = new User();
        String username = text(body.get("username"));
        if (!StringUtils.hasText(username)) username = "sub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        if (userRepository.findByUsername(username).isPresent()) throw new IllegalArgumentException("账号已存在: " + username);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(UserRole.BOSS);
        user.setNickname(StringUtils.hasText(text(body.get("nickname"))) ? text(body.get("nickname")) : "授权员工");
        user.setPhone(phone);
        user.setParentUserId(bossId);
        user.setSubRole(role);
        user.setStatus("正常");
        return userRepository.save(user);
    }

    private void verifyCode(String code) {
        boolean dev = environment.acceptsProfiles(Profiles.of("dev"));
        boolean mockEnabled = Boolean.parseBoolean(environment.getProperty("aliyun.sms.mock-enabled", "false"));
        if (!dev || !mockEnabled || !StringUtils.hasText(devVerificationCode)) {
            throw new IllegalStateException("短信验证码服务尚未配置");
        }
        if (!devVerificationCode.equals(code)) throw new IllegalArgumentException("验证码不正确");
    }

    private SubAccount owned(Long id, Long bossId) {
        return subAccountRepository.findByIdAndParentId(id, bossId).orElseThrow(() ->
                subAccountRepository.existsById(id)
                        ? new ForbiddenBusinessException("无权操作其他老板的子账号")
                        : new IllegalArgumentException("子账号不存在: " + id));
    }

    private Long requireBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user
                && user.id() != null && UserRole.BOSS.equals(user.role())) return user.id();
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }

    private void rejectOtherParent(Long parentId, Long bossId) {
        if (parentId != null && !bossId.equals(parentId)) throw new ForbiddenBusinessException("无权访问其他老板的子账号");
    }

    private String normalizeRole(String role) {
        String normalized = StringUtils.hasText(role) ? role.trim().toUpperCase() : "OPERATOR";
        if (!ROLES.contains(normalized)) throw new IllegalArgumentException("不支持的子账号角色: " + normalized);
        return normalized;
    }

    private Long toLong(Object value) { return value == null ? null : Long.valueOf(value.toString()); }
    private String text(Object value) { return value == null ? "" : value.toString().trim(); }
}

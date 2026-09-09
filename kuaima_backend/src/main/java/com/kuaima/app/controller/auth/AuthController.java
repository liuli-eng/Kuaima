package com.kuaima.app.controller.auth;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.constant.CertificationStatus;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.dto.WechatLoginDto;
import com.kuaima.app.security.model.LoginUser;
import com.kuaima.app.security.util.JwtUtil;
import com.kuaima.app.service.SmsService;
import com.kuaima.app.wechat.service.WechatService;
import com.kuaima.app.wechat.service.WechatService.WechatUserInfo;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "微信登录、JWT 鉴权、短信验证码")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final WechatService wechatService;
    private final SmsService smsService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          WechatService wechatService,
                          SmsService smsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.wechatService = wechatService;
        this.smsService = smsService;
    }

    private Map<String, Object> buildTokenResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole(), user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("phone", user.getPhone());
        data.put("certStatus", user.getCertStatus());
        return data;
    }

    @Operation(summary = "微信小程序登录", description = "小程序端 wx.login() 获取 code，后端调用微信 jscode2session 换取 openid；用户不存在时按所选身份自动注册，老用户以本次选择的身份为准直接切换。若传入 phoneCode，后端调用微信 getuserphonenumber 换取手机号并保存")
    @PostMapping("/wechat/login")
    @Transactional
    public Result<Map<String, Object>> wechatLogin(@RequestBody WechatLoginDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getCode())) {
            return Result.error(400, "微信 code 不能为空");
        }
        // 身份：缺省按员工(USER)处理，非法值报错
        String role = StringUtils.hasText(dto.getRole()) ? dto.getRole() : UserRole.USER;
        if (!UserRole.isValid(role)) {
            return Result.error(400, "身份不合法：仅支持 BOSS(老板) / USER(员工)");
        }
        WechatUserInfo info = wechatService.loginByCode(dto.getCode());
        if (info.openid() == null) {
            return Result.error(401, "微信登录失败：未获取到 openid");
        }
        // 手机号动态令牌非空时换取手机号
        final String phone = StringUtils.hasText(dto.getPhoneCode())
                ? wechatService.getPhoneNumber(dto.getPhoneCode())
                : null;
        // 按 openid 查找用户，不存在则按所选身份自动注册
        User user = userRepository.findByOpenid(info.openid()).orElseGet(() -> {
            User newUser = new User();
            // 微信用户无密码，生成不可登录的随机密码；完整 openid 避免仅截取后 16 位造成用户名碰撞
            newUser.setUsername("wx_" + info.openid());
            newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            newUser.setRole(role);
            newUser.setOpenid(info.openid());
            newUser.setNickname(info.nickname() != null ? info.nickname() : dto.getNickname());
            newUser.setAvatar(info.avatar() != null ? info.avatar() : dto.getAvatar());
            newUser.setPhone(phone);
            return userRepository.save(newUser);
        });
        // 老用户身份以本次选择为准，直接切换
        if (!role.equals(user.getRole())) {
            user.setRole(role);
            userRepository.save(user);
        }
        // 老用户手机号为空时补全
        if (phone != null && !phone.equals(user.getPhone())) {
            user.setPhone(phone);
            userRepository.save(user);
        }
        return Result.success(buildTokenResponse(user));
    }

    /**
     * 当前登录用户完整资料：GET /auth/me
     * 从 JWT 解析出 uid 后反查 User 实体返回完整 profile
     */
    @Operation(summary = "获取当前用户", description = "从 JWT 解析出 uid 后反查 User 实体，返回当前登录用户的完整资料")
    @GetMapping("/me")
    public Result<User> me(Authentication authentication) {
        Long uid = currentUserId(authentication);
        if (uid == null) {
            return Result.success(null);
        }
        return Result.success(userRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + uid)));
    }

    /**
     * 切换身份：POST /auth/switch-role?role=BOSS|USER
     * 更新当前用户 role，并签发包含新 role 的新 JWT
     */
    @Operation(summary = "切换用户身份", description = "更新当前用户 role，并签发包含新 role 的新 JWT accessToken")
    @PostMapping("/switch-role")
    @Transactional
    public Result<Map<String, Object>> switchRole(@RequestParam String role,
                                                    Authentication authentication) {
        if (!UserRole.isValid(role)) {
            return Result.error(400, "身份不合法：仅支持 BOSS(老板) / USER(员工)");
        }
        Long uid = currentUserId(authentication);
        if (uid == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + uid));
        if (!role.equals(user.getRole())) {
            user.setRole(role);
            user = userRepository.save(user);
        }
        return Result.success(buildTokenResponse(user));
    }

    /**
     * 注销账号（软删除）：POST /auth/cancel?userId={id}&reason={reason}
     * 仅将 User.status 置为「注销」，不物理删除，保留历史数据
     */
    @Operation(summary = "账号注销", description = "软删除：将 User.status 置为「注销」并记录注销原因，不物理删除，保留历史数据")
    @PostMapping("/cancel")
    @Transactional
    public Result<User> cancel(@RequestParam Long userId,
                               @RequestParam(required = false) String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));
        user.setStatus("注销");
        if (StringUtils.hasText(reason)) {
            user.setRemark(StringUtils.hasText(user.getRemark())
                    ? user.getRemark() + "；注销原因：" + reason
                    : "注销原因：" + reason);
        }
        return Result.success(userRepository.save(user));
    }

    /**
     * 发送短信验证码：POST /auth/sms/send?phone=13800000000
     * 需登录，60秒重发限制，验证码5分钟有效
     */
    @Operation(summary = "发送短信验证码", description = "需登录，向指定手机号发送6位验证码；60秒重发限制，5分钟有效")
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        String err = smsService.sendCode(phone);
        if (err != null) {
            return Result.error(400, err);
        }
        return Result.success();
    }

    /**
     * 校验短信验证码：POST /auth/sms/verify?phone=13800000000&code=123456
     * 无需登录，校验通过后删除验证码
     */
    @Operation(summary = "校验短信验证码", description = "无需登录，校验手机号+验证码，通过后验证码自动失效")
    @PostMapping("/sms/verify")
    public Result<Map<String, Object>> verifySmsCode(@RequestParam String phone,
                                                      @RequestParam String code) {
        boolean ok = smsService.verifyCode(phone, code);
        if (!ok) {
            return Result.error(400, "验证码错误或已过期");
        }
        // 当前业务规则：手机号验证通过即视为个人认证通过。
        var users = userRepository.findByPhone(phone);
        for (var user : users) {
            boolean changed = false;
            if (!CertificationStatus.APPROVED.equals(user.getRealnameStatus())) {
                user.setRealnameStatus(CertificationStatus.APPROVED);
                changed = true;
            }
            if (!"已通过".equals(user.getCertStatus())) {
                user.setCertStatus("已通过");
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("phone", phone);
        data.put("verified", true);
        return Result.success(data);
    }

    /** 从 SecurityContext 提取当前登录用户 ID，缺失返回 null */
    private Long currentUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser.id();
        }
        return null;
    }
}

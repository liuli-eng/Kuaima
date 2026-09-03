package com.suma.app.controller.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.common.Result;
import com.suma.app.domain.user.constant.UserRole;
import com.suma.app.domain.user.entity.User;
import com.suma.app.domain.user.repository.UserRepository;
import com.suma.app.security.dto.WechatLoginDto;
import com.suma.app.security.model.LoginUser;
import com.suma.app.security.util.JwtUtil;
import com.suma.app.wechat.service.WechatService;
import com.suma.app.wechat.service.WechatService.WechatUserInfo;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final WechatService wechatService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          WechatService wechatService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.wechatService = wechatService;
    }

    private Map<String, Object> buildTokenResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole(), user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        return data;
    }

    @PostMapping("/wechat/login")
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
            // 微信用户无密码，生成不可登录的随机密码；username 用 wx_ 前缀保证唯一
            newUser.setUsername("wx_" + info.openid().substring(Math.max(0, info.openid().length() - 16)));
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

    @GetMapping("/me")
    public Result<String> me(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return Result.success(loginUser.username());
        }
        return Result.success(authentication.getName());
    }
}

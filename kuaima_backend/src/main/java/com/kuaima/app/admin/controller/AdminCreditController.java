package com.kuaima.app.admin.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.user.service.CreditScoreService;
import com.kuaima.app.security.model.LoginUser;

import jakarta.persistence.EntityNotFoundException;

/** 后台信用分/零工星级分查询及人工调整。 */
@RestController
@RequestMapping("/admin/credit")
public class AdminCreditController {
    private final UserRepository users;
    private final CreditScoreService scores;

    public AdminCreditController(UserRepository users, CreditScoreService scores) {
        this.users = users;
        this.scores = scores;
    }

    @GetMapping("/{userId}")
    public Result<Map<String, Object>> detail(@PathVariable Long userId, Authentication authentication) {
        admin(authentication, false);
        User user = users.findById(userId).orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", userId);
        data.put("creditScore", user.getCreditScore() == null ? 0 : user.getCreditScore());
        data.put("starScore", user.getStarScore() == null ? 0 : user.getStarScore());
        data.put("creditFlows", scores.flows(userId, CreditScoreService.BOSS_CREDIT));
        data.put("starFlows", scores.flows(userId, CreditScoreService.WORKER_STAR));
        return Result.success(data);
    }

    @PostMapping("/{userId}/adjust")
    public Result<Map<String, Object>> adjust(@PathVariable Long userId, @RequestBody Map<String, Object> body,
                                               Authentication authentication) {
        LoginUser operator = admin(authentication, true);
        String type = text(body, "scoreType", CreditScoreService.BOSS_CREDIT);
        int delta = number(body.get("delta"));
        String reason = text(body, "reason", "管理员人工调整");
        String ruleCode = text(body, "ruleCode", "ADMIN_MANUAL_ADJUST");
        String bizId = text(body, "bizId", String.valueOf(System.currentTimeMillis()));
        scores.adjust(userId, type, delta, ruleCode, "ADMIN", "ADMIN:" + operator.id() + ":" + userId + ":" + bizId, reason);
        return detail(userId, authentication);
    }

    private LoginUser admin(Authentication authentication, boolean write) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)
                || user.role() == null || !user.role().startsWith("ADMIN_")) {
            throw new ForbiddenBusinessException("仅管理员可操作");
        }
        if (write && user.role().endsWith("VIEWER")) throw new ForbiddenBusinessException("当前管理员无信用分管理权限");
        return user;
    }

    private String text(Map<String, Object> body, String key, String fallback) {
        Object value = body == null ? null : body.get(key);
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
    }

    private int number(Object value) {
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { throw new IllegalArgumentException("delta 必须是整数"); }
    }
}

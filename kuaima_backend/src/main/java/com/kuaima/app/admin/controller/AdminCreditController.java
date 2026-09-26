package com.kuaima.app.admin.controller;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.entity.CreditFlow;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.user.repository.CreditFlowRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.service.CreditScoreService;
import com.kuaima.app.security.model.LoginUser;

import jakarta.persistence.EntityNotFoundException;

/** 后台信用分/零工星级分查询及人工调整。 */
@RestController
@RequestMapping("/admin/credit")
public class AdminCreditController {
    private final UserRepository users;
    private final CreditScoreService scores;
    private final CreditFlowRepository flows;

    public AdminCreditController(UserRepository users, CreditScoreService scores) {
        this(users, scores, null);
    }

    public AdminCreditController(UserRepository users, CreditScoreService scores, CreditFlowRepository flows) {
        this.users = users;
        this.scores = scores;
        this.flows = flows;
    }

    @GetMapping("/users")
    public Result<Page<Map<String, Object>>> users(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String role,
                                                    @RequestParam(required = false) String level,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    Authentication authentication) {
        admin(authentication, false);
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedRole = normalizeRole(role);
        List<User> candidates = normalizedRole == null
                ? users.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                    .filter(user -> UserRole.USER.equals(user.getRole()) || UserRole.BOSS.equals(user.getRole())).toList()
                : users.findByRole(normalizedRole);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User user : candidates) {
            String name = firstText(user.getRealName(), user.getNickname(), user.getUsername(), user.getPhone());
            if (kw != null && !containsAny(kw, user.getId(), name, user.getUsername(), user.getNickname(), user.getPhone())) continue;
            String scoreType = CreditScoreService.BOSS_CREDIT;
            int score = value(user.getCreditScore());
            String levelKey = levelKey(score);
            if (level != null && !level.isBlank() && !levelKey.equals(level)) continue;
            List<CreditFlow> history = flows == null ? List.of()
                    : flows.findByUserIdAndScoreTypeOrderByTimestampDesc(user.getId(), scoreType);
            int added = history.stream().mapToInt(CreditFlow::getDelta).filter(delta -> delta > 0).sum();
            int deducted = history.stream().mapToInt(CreditFlow::getDelta).filter(delta -> delta < 0).map(Math::abs).sum();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", user.getId()); row.put("role", UserRole.USER.equals(user.getRole()) ? "worker" : "boss");
            row.put("name", name); row.put("phone", user.getPhone()); row.put("score", score);
            row.put("level", levelKey); row.put("addTotal", added); row.put("subTotal", deducted);
            row.put("scoreType", scoreType);
            rows.add(row);
        }
        int from = Math.min(Math.max(page, 0) * Math.max(size, 1), rows.size());
        int to = Math.min(from + Math.max(size, 1), rows.size());
        Page<Map<String, Object>> result = new PageImpl<>(rows.subList(from, to), PageRequest.of(Math.max(page, 0), Math.max(size, 1)), rows.size());
        return Result.success(result, result.getNumber(), result.getTotalElements());
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

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) return null;
        return switch (role.toLowerCase(Locale.ROOT)) {
            case "worker", "user", "零工" -> UserRole.USER;
            case "boss", "老板" -> UserRole.BOSS;
            default -> null;
        };
    }

    private String levelKey(int score) {
        if (score >= 90) return "excellent";
        if (score >= 75) return "good";
        if (score >= 60) return "medium";
        return "low";
    }

    private int value(Integer score) { return score == null ? 0 : score; }

    private String firstText(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "-";
    }

    private boolean containsAny(String keyword, Object... values) {
        for (Object value : values) if (value != null && String.valueOf(value).toLowerCase(Locale.ROOT).contains(keyword)) return true;
        return false;
    }
}

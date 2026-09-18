package com.kuaima.app.controller.boss;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.enterprise.entity.Enterprise;
import com.kuaima.app.domain.enterprise.entity.EnterpriseMember;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;
import com.kuaima.app.domain.enterprise.service.EnterpriseContextService;
import com.kuaima.app.security.util.JwtUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 老板端企业上下文：员工和 OWNER 通过此接口选择当前企业。 */
@RestController
@RequestMapping("/boss/contexts")
public class BossContextController {
    private final EnterpriseContextService contexts;
    private final EnterpriseRepository enterprises;
    private final JwtUtil jwtUtil;

    public BossContextController(EnterpriseContextService contexts, EnterpriseRepository enterprises,
                                 JwtUtil jwtUtil) {
        this.contexts = contexts;
        this.enterprises = enterprises;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public Result<Map<String, Object>> list(Authentication authentication) {
        EnterpriseContextService.Context current = contexts.require(authentication);
        List<Map<String, Object>> records = contexts.memberships(current.user().getId()).stream()
                .map(member -> enterpriseView(member, member.getEnterpriseId(), current.enterprise().getId()))
                .toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentEnterpriseId", current.enterprise().getId());
        data.put("currentMemberRole", current.member().getMemberRole());
        data.put("enterprises", records);
        return Result.success(data);
    }

    public record SwitchRequest(Long enterpriseId) {}

    @PutMapping("/current")
    public Result<Map<String, Object>> switchCurrent(@RequestBody SwitchRequest request,
                                                      Authentication authentication) {
        EnterpriseContextService.Context current = contexts.require(authentication);
        if (request == null || request.enterpriseId() == null) {
            throw new IllegalArgumentException("enterpriseId 不能为空");
        }
        EnterpriseMember target = contexts.memberships(current.user().getId()).stream()
                .filter(member -> Objects.equals(member.getEnterpriseId(), request.enterpriseId()))
                .findFirst().orElseThrow(() -> new com.kuaima.app.common.ForbiddenBusinessException("无权访问该企业"));
        Enterprise enterprise = enterprises.findById(target.getEnterpriseId())
                .orElseThrow(() -> new com.kuaima.app.common.ForbiddenBusinessException("企业不存在"));
        String token = jwtUtil.generateAccessToken(current.user().getUsername(), "BOSS", current.user().getId(),
                current.user().getId(), enterprise.getId(), target.getMemberRole());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", token);
        data.put("enterpriseId", enterprise.getId());
        data.put("memberRole", target.getMemberRole());
        data.put("companyCode", enterprise.getCompanyCode());
        data.put("companyName", enterprise.getCompanyName());
        return Result.success(data);
    }

    private Map<String, Object> enterpriseView(EnterpriseMember member, Long enterpriseId, Long currentId) {
        Enterprise enterprise = enterprises.findById(enterpriseId).orElse(null);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", enterpriseId);
        view.put("companyCode", enterprise == null ? "" : enterprise.getCompanyCode());
        view.put("companyName", enterprise == null ? "" : enterprise.getCompanyName());
        view.put("memberRole", member.getMemberRole());
        view.put("current", Objects.equals(enterpriseId, currentId));
        return view;
    }
}

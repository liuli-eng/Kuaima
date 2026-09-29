package com.kuaima.app.controller.service;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.social.dto.SocialGroupJoinRequest;
import com.kuaima.app.domain.social.dto.SocialGroupView;
import com.kuaima.app.domain.social.service.SocialGroupService;
import com.kuaima.app.security.model.LoginUser;

/**
 * 零工端社群列表和扫码加入确认。
 */
@RestController
@RequestMapping("/social-groups")
@Tag(name = "社群", description = "用户社群/工会管理")
public class SocialGroupController {

    private final SocialGroupService service;

    public SocialGroupController(SocialGroupService service) {
        this.service = service;
    }

    @Operation(summary = "零工可加入社群", description = "只返回 ACTIVE 且零工可见、未删除的群")
    @GetMapping
    public Result<List<SocialGroupView>> listGroups(
            @RequestParam(defaultValue = "WORKER") String role,
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Page<SocialGroupView> result = service.list(current(authentication), role, status, page, size);
        return Result.success(result.getContent(), page, result.getTotalElements());
    }

    @Operation(summary = "确认加入社群", description = "JWT 识别零工；实名认证后确认已扫码，重复请求幂等")
    @PostMapping("/{id}/join")
    public Result<SocialGroupView> join(@PathVariable Long id,
                                        @RequestBody SocialGroupJoinRequest request,
                                        Authentication authentication) {
        return Result.success(service.join(id, request, current(authentication)));
    }

    private LoginUser current(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        throw new ForbiddenBusinessException("请先登录");
    }
}

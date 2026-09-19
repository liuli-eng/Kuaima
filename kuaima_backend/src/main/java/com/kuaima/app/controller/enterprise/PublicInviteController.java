package com.kuaima.app.controller.enterprise;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.enterprise.entity.Enterprise;
import com.kuaima.app.domain.enterprise.entity.EnterpriseInvite;
import com.kuaima.app.domain.enterprise.entity.EnterpriseJoinApply;
import com.kuaima.app.domain.enterprise.repository.EnterpriseInviteRepository;
import com.kuaima.app.domain.enterprise.repository.EnterpriseJoinApplyRepository;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 公开邀请接口（无需登录）：微信/浏览器通过邀请码查询企业信息并提交入企申请。
 * 安全配置已放行 /public/enterprise/**。
 */
@RestController
@RequestMapping("/public/enterprise")
@Tag(name = "公开-企业邀请", description = "邀请码查询、提交入企申请（无需登录）")
@RequiredArgsConstructor
public class PublicInviteController {

    private final EnterpriseInviteRepository inviteRepository;
    private final EnterpriseJoinApplyRepository applyRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Operation(summary = "根据邀请码查询邀请信息与企业名称", description="用于入企申请页展示「您正在加入 XX 企业」")
    @GetMapping("/invite")
    public Result<Map<String, Object>> getInviteInfo(@RequestParam String code) {
        EnterpriseInvite invite = inviteRepository.findByInviteCode(code).orElse(null);
        if (invite == null) {
            return Result.error(404, "邀请码无效或已失效");
        }
        Enterprise enterprise = enterpriseRepository.findById(invite.getEnterpriseId()).orElse(null);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("inviteCode", invite.getInviteCode());
        data.put("enterpriseName", enterprise != null ? enterprise.getCompanyName() : "未知企业");
        data.put("inviteRole", invite.getInviteRole());
        data.put("status", invite.getStatus());
        return Result.success(data);
    }

    @Operation(summary = "提交入企申请", description="通过邀请码提交入企申请，创建后状态为 PENDING 待老板审批")
    @PostMapping("/apply")
    public Result<Map<String, Object>> submitApply(@RequestBody Map<String, String> body) {
        String code = body == null ? null : body.get("code");
        if (!StringUtils.hasText(code)) {
            return Result.error(400, "邀请码不能为空");
        }
        EnterpriseInvite invite = inviteRepository.findByInviteCode(code.trim()).orElse(null);
        if (invite == null) {
            return Result.error(404, "邀请码无效或已失效");
        }
        String name = body.get("name");
        String phone = body.get("phone");
        if (!StringUtils.hasText(name) || name.trim().isEmpty()) {
            return Result.error(400, "请输入姓名");
        }
        if (!StringUtils.hasText(phone) || !phone.trim().matches("1\\d{10}")) {
            return Result.error(400, "请输入正确的11位手机号");
        }
        EnterpriseJoinApply apply = new EnterpriseJoinApply();
        apply.setEnterpriseId(invite.getEnterpriseId());
        apply.setName(name.trim());
        apply.setPhone(phone.trim());
        apply.setNote(body.get("note"));
        apply.setApplyRole(invite.getInviteRole());
        apply.setSource("邀请二维码");
        apply.setStatus("PENDING");
        EnterpriseJoinApply saved = applyRepository.save(apply);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applyId", saved.getId());
        data.put("status", saved.getStatus());
        data.put("message", "申请已提交，请等待企业审核");
        return Result.success(data);
    }
}

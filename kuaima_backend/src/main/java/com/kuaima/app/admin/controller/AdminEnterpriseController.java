package com.kuaima.app.admin.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.common.util.QrCodeUtils;
import com.kuaima.app.domain.enterprise.entity.Enterprise;
import com.kuaima.app.domain.enterprise.entity.EnterpriseInvite;
import com.kuaima.app.domain.enterprise.repository.EnterpriseInviteRepository;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 后台-企业邀请二维码。
 * admin 账号是平台级视角、没有「当前企业」上下文，因此二维码必须按所选企业生成。
 */
@RestController
@RequestMapping("/admin/enterprises")
@Tag(name = "后台-企业邀请", description = "企业列表、按企业生成邀请二维码")
public class AdminEnterpriseController {

    /** 企业级二维码不绑定具体手机号，使用固定占位号码复用同一条邀请记录。 */
    private static final String PUBLIC_INVITE_PHONE = "00000000000";

    private final EnterpriseRepository enterpriseRepository;
    private final EnterpriseInviteRepository inviteRepository;

    public AdminEnterpriseController(EnterpriseRepository enterpriseRepository,
                                     EnterpriseInviteRepository inviteRepository) {
        this.enterpriseRepository = enterpriseRepository;
        this.inviteRepository = inviteRepository;
    }

    @GetMapping
    @Operation(summary = "企业列表（邀请二维码选择企业用）")
    public Result<List<Map<String, Object>>> list() {
        List<Map<String, Object>> list = enterpriseRepository.findAll().stream()
                .sorted(Comparator.comparing(Enterprise::getId).reversed())
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", e.getId());
                    item.put("companyName", e.getCompanyName());
                    item.put("companyCode", e.getCompanyCode());
                    return item;
                })
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @GetMapping("/{enterpriseId}/invite-qr")
    @Operation(summary = "按企业生成邀请二维码（邀请码 + 链接 + 企业名称 + base64 二维码图）")
    public Result<Map<String, Object>> inviteQr(@PathVariable Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId).orElse(null);
        if (enterprise == null) {
            return Result.error(404, "企业不存在");
        }

        List<EnterpriseInvite> existing = inviteRepository
                .findByEnterpriseIdAndPhoneAndStatusOrderByIdDesc(enterpriseId, PUBLIC_INVITE_PHONE, "PENDING");
        EnterpriseInvite invite = existing.isEmpty() ? createInvite(enterprise) : existing.get(0);

        String link = "https://kuaima.com/invite?code=" + invite.getInviteCode();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("inviteCode", invite.getInviteCode());
        data.put("link", link);
        data.put("enterpriseId", enterprise.getId());
        data.put("enterpriseName", enterprise.getCompanyName());
        data.put("qrImage", QrCodeUtils.generateBase64Png(link, 300));
        return Result.success(data);
    }

    private EnterpriseInvite createInvite(Enterprise enterprise) {
        EnterpriseInvite invite = new EnterpriseInvite();
        invite.setEnterpriseId(enterprise.getId());
        invite.setInviterId(enterprise.getCreateBy() != null ? enterprise.getCreateBy() : 0L);
        invite.setPhone(PUBLIC_INVITE_PHONE);
        invite.setName("邀请二维码");
        invite.setInviteRole("STAFF");
        invite.setInviteCode(UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        invite.setStatus("PENDING");
        return inviteRepository.save(invite);
    }
}
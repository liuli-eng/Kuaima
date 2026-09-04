package com.kuaima.app.controller.finance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.invite.entity.InviteCode;
import com.kuaima.app.domain.invite.entity.InviteRelation;
import com.kuaima.app.domain.invite.repository.InviteCodeRepository;
import com.kuaima.app.domain.invite.repository.InviteRelationRepository;

/**
 * 邀请码与邀请记录。
 */
@RestController
@RequestMapping("/invite")
public class InviteController {

    private final InviteCodeRepository inviteCodeRepository;
    private final InviteRelationRepository inviteRelationRepository;

    public InviteController(InviteCodeRepository inviteCodeRepository,
                            InviteRelationRepository inviteRelationRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.inviteRelationRepository = inviteRelationRepository;
    }

    /** 邀请码（首次自动生成）：GET /invite/code?userId=1 */
    @GetMapping("/code")
    @Transactional
    public Result<Map<String, Object>> getCode(@RequestParam Long userId) {
        InviteCode inviteCode = inviteCodeRepository.findByUserId(userId).orElseGet(() -> {
            InviteCode code = new InviteCode();
            code.setUserId(userId);
            code.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
            return inviteCodeRepository.save(code);
        });
        Map<String, Object> data = new HashMap<>();
        data.put("code", inviteCode.getCode());
        return Result.success(data);
    }

    /** 海报数据：GET /invite/poster?userId=1 */
    @GetMapping("/poster")
    @Transactional
    public Result<Map<String, Object>> getPoster(@RequestParam Long userId) {
        InviteCode inviteCode = inviteCodeRepository.findByUserId(userId).orElseGet(() -> {
            InviteCode code = new InviteCode();
            code.setUserId(userId);
            code.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
            return inviteCodeRepository.save(code);
        });
        long invitedCount = inviteRelationRepository.findByInviterId(userId).size();
        Map<String, Object> data = new HashMap<>();
        data.put("code", inviteCode.getCode());
        data.put("invitedCount", invitedCount);
        data.put("posterUrl", "");
        return Result.success(data);
    }

    /** 邀请记录：GET /invite/relations?userId=1 */
    @GetMapping("/relations")
    public Result<List<InviteRelation>> listRelations(@RequestParam Long userId) {
        return Result.success(inviteRelationRepository.findByInviterId(userId));
    }
}

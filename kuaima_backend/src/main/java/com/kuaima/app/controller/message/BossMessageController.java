package com.kuaima.app.controller.message;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.message.constant.MessageType;
import com.kuaima.app.domain.message.entity.Message;
import com.kuaima.app.domain.message.model.BossMessageModels.MessagePreview;
import com.kuaima.app.domain.message.model.BossMessageModels.SettlementPreview;
import com.kuaima.app.domain.message.model.BossMessageModels.Summary;
import com.kuaima.app.domain.message.model.BossMessageModels.MessageItem;
import com.kuaima.app.domain.message.repository.MessageRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.service.CertificationService;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 老板消息首页聚合及历史消息分页接口。 */
@RestController
@RequestMapping("/boss/message")
@Tag(name = "老板-消息", description = "老板消息首页卡片与历史消息")
public class BossMessageController {
    private final MessageRepository messageRepository;
    private final CertificationService certificationService;
    private final SettlementRespository settlementRepository;

    public BossMessageController(MessageRepository messageRepository,
                                 CertificationService certificationService,
                                 SettlementRespository settlementRepository) {
        this.messageRepository = messageRepository;
        this.certificationService = certificationService;
        this.settlementRepository = settlementRepository;
    }

    @GetMapping("/summary")
    @Operation(summary = "老板消息首页聚合", description = "返回企业认证状态及提醒、系统通知摘要、报名人数/未读数、最新结算日期金额和状态")
    public Result<Summary> summary(Authentication authentication) {
        Long bossId = requireBossId(authentication);
        var eligibility = certificationService.publishEligibility(bossId);
        String enterpriseStatus = String.valueOf(eligibility.get("enterpriseStatus"));
        long unread = messageRepository.countByUserIdAndRoleAndReadFlagFalse(bossId, UserRole.BOSS);
        long signupUnread = messageRepository.countByUserIdAndRoleAndTypeAndReadFlagFalse(bossId, UserRole.BOSS, MessageType.ORDER_APPLY);
        long signupCount = messageRepository.findByUserIdAndRoleAndTypeOrderByIdDesc(bossId, UserRole.BOSS, MessageType.ORDER_APPLY, PageRequest.of(0, 1)).getTotalElements();
        Message system = messageRepository.findFirstByUserIdAndRoleAndTypeOrderByIdDesc(bossId, UserRole.BOSS, "SYSTEM_NOTICE").orElse(null);
        Message signup = messageRepository.findFirstByUserIdAndRoleAndTypeOrderByIdDesc(bossId, UserRole.BOSS, MessageType.ORDER_APPLY).orElse(null);
        Settlement settlement = settlementRepository.findByBossId(bossId, PageRequest.of(0, 1)).stream().findFirst().orElse(null);
        boolean enterpriseCertUnread = !"APPROVED".equals(enterpriseStatus);
        List<MessageItem> items = new ArrayList<>();
        // 认证提醒是状态型提醒：认证通过后不再展示。
        if (enterpriseCertUnread) {
            items.add(new MessageItem("enterprise-certification", "企业认证提醒",
                    "曝光加权·优先推荐熟练零工接单", "ENTERPRISE_CERTIFICATION",
                    "PENDING".equals(enterpriseStatus),
                    "PENDING".equals(enterpriseStatus) ? "审核中" : "立即认证", null, null));
        }
        if (system != null) {
            items.add(new MessageItem("system-notice", "系统通知", system.getContent(),
                    system.getType(), !Boolean.TRUE.equals(system.getReadFlag()), "查看", system.getBizId(), system.getCreateTime()));
        }
        if (signup != null) {
            items.add(new MessageItem("signup-notice", "报名通知", signup.getContent(),
                    signup.getType(), signupUnread > 0, "查看", signup.getBizId(), signup.getCreateTime()));
        }
        if (settlement != null) {
            items.add(new MessageItem("settlement", "结算消息", settlement.getStatus(),
                    "SETTLEMENT", false, "查看", settlement.getId(), settlement.getPayTime()));
        }
        return Result.success(new Summary(enterpriseStatus, enterpriseCertUnread, unread,
                signupUnread, signupCount, preview(system), preview(signup), settlementPreview(settlement), items));
    }

    @GetMapping("/history")
    @Operation(summary = "老板历史消息分页", description = "按当前老板 JWT 查询历史消息，支持 read/type 筛选，page 从 0 开始")
    public Result<List<Message>> history(@RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, Authentication authentication) {
        Long bossId = requireBossId(authentication);
        Page<Message> result = messageRepository.findByUserIdAndEffectiveRole(bossId, UserRole.BOSS,
                MessageType.BOSS_MESSAGE_TYPES, MessageType.USER_MESSAGE_TYPES,
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)));
        if (type != null && !type.isBlank()) {
            result = read == null
                    ? messageRepository.findByUserIdAndRoleAndTypeOrderByIdDesc(bossId, UserRole.BOSS, type.trim().toUpperCase(), PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)))
                    : messageRepository.findByUserIdAndRoleAndTypeAndReadFlagOrderByIdDesc(bossId, UserRole.BOSS, type.trim().toUpperCase(), read, PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)));
        } else if (read != null) {
            result = messageRepository.findByUserIdAndEffectiveRoleAndReadFlag(bossId, UserRole.BOSS, read,
                    MessageType.BOSS_MESSAGE_TYPES, MessageType.USER_MESSAGE_TYPES,
                    PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)));
        }
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    private MessagePreview preview(Message m) { return m == null ? null : new MessagePreview(m.getId(), m.getTitle(), m.getContent(), m.getType(), m.getReadFlag(), m.getCreateTime(), m.getBizId()); }
    private SettlementPreview settlementPreview(Settlement s) { return s == null ? null : new SettlementPreview(s.getId(), s.getStatus(), s.getWage(), s.getTotalAmount(), s.getPayTime(), s.getOrderId(), s.getItemId()); }
    private Long requireBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser u && UserRole.BOSS.equals(u.role()) && u.id() != null) return u.id();
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }
}

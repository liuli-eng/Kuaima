package com.kuaima.app.domain.expense.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.expense.dto.ExpenseApplicationRequest;
import com.kuaima.app.domain.expense.entity.ExpenseApplication;
import com.kuaima.app.domain.expense.repository.ExpenseApplicationRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ExpenseApplicationService {
    private static final List<String> TYPES = List.of("TRANSPORT", "MEAL", "MATERIAL", "INSURANCE", "OTHER");
    private static final List<String> ALLOWED_ORDER_STATUSES = List.of(
            BossStatus.ORDER_PENDING_SETTLE, BossStatus.ORDER_COMPLETED);

    private final ExpenseApplicationRepository applications;
    private final BossOrderRespository orders;
    private final UserRepository users;

    public ExpenseApplicationService(ExpenseApplicationRepository applications, BossOrderRespository orders,
                                     UserRepository users) {
        this.applications = applications;
        this.orders = orders;
        this.users = users;
    }

    @Transactional
    public ExpenseApplication create(ExpenseApplicationRequest request, LoginUser user, String headerKey) {
        requireBoss(user);
        String type = request.getType().trim().toUpperCase(Locale.ROOT);
        if (!TYPES.contains(type)) throw new IllegalArgumentException("type 参数无效");
        if (request.getAmount().signum() <= 0) throw new IllegalArgumentException("报销金额必须大于0");
        String reason = request.getReason().trim();
        if (reason.isEmpty()) throw new IllegalArgumentException("报销事由不能为空");
        List<String> attachments = request.getAttachments() == null ? List.of()
                : request.getAttachments().stream().map(String::trim).toList();
        String key = firstNonBlank(headerKey, request.getIdempotencyKey());
        if (key == null) key = fingerprint(user.id(), request, type, reason, attachments);
        if (key.length() > 128) throw new IllegalArgumentException("幂等键不能超过128个字符");
        var existing = applications.findByBossIdAndIdempotencyKey(user.id(), key);
        if (existing.isPresent()) return existing.get();

        BossOrder order = orders.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("订单不存在: " + request.getOrderId()));
        if (!user.id().equals(order.getCreateBy())) throw new IllegalArgumentException("无权为该订单申请报销");
        if (!ALLOWED_ORDER_STATUSES.contains(order.getOrderStatus())) {
            throw new IllegalArgumentException("当前订单状态不允许申请报销");
        }
        ExpenseApplication application = new ExpenseApplication();
        application.setBossId(user.id()); application.setOrderId(order.getId());
        application.setOrderTitle(order.getOrderTitle()); application.setType(type);
        application.setAmount(request.getAmount()); application.setReason(reason);
        application.setAttachments(JSON.toJSONString(attachments, JSONWriter.Feature.WriteNullListAsEmpty));
        application.setManualReview(request.getAmount().compareTo(new BigDecimal("500.00")) > 0);
        application.setIdempotencyKey(key); application.setStatus("PENDING");
        application.setCreateTime(LocalDateTime.now());
        try {
            return applications.save(application);
        } catch (org.springframework.dao.DataIntegrityViolationException duplicate) {
            return applications.findByBossIdAndIdempotencyKey(user.id(), key).orElseThrow(() -> duplicate);
        }
    }

    public Page<ExpenseApplication> list(LoginUser user, String status, Pageable pageable) {
        requireBoss(user);
        if (status != null && !List.of("PENDING", "APPROVED", "REJECTED", "PAID").contains(status)) {
            throw new IllegalArgumentException("status 参数无效");
        }
        return applications.searchByBoss(user.id(), status, pageable);
    }

    private void requireBoss(LoginUser user) {
        if (user == null || user.id() == null || !"BOSS".equals(user.role())) {
            throw new com.kuaima.app.common.ForbiddenBusinessException("仅老板可以申请费用报销");
        }
        User account = users.findById(user.id())
                .orElseThrow(() -> new com.kuaima.app.common.ForbiddenBusinessException("当前用户不存在"));
        if (!UserRole.isBossIdentity(account)) {
            throw new com.kuaima.app.common.ForbiddenBusinessException("仅老板身份可以申请费用报销");
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first.trim();
        if (second != null && !second.isBlank()) return second.trim();
        return null;
    }

    private String fingerprint(Long userId, ExpenseApplicationRequest request, String type,
                               String reason, List<String> attachments) {
        String raw = userId + "|" + request.getOrderId() + "|" + type + "|" + request.getAmount()
                + "|" + reason + "|" + String.join("\u001f", attachments);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder(64);
            for (byte value : digest) out.append(String.format("%02x", value));
            return out.toString();
        } catch (Exception e) {
            throw new IllegalStateException("生成幂等键失败", e);
        }
    }
}

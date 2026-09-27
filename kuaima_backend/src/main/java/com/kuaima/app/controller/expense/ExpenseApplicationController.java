package com.kuaima.app.controller.expense;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.expense.dto.ExpenseApplicationRequest;
import com.kuaima.app.domain.expense.entity.ExpenseApplication;
import com.kuaima.app.domain.expense.service.ExpenseApplicationService;
import com.kuaima.app.security.model.LoginUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/expenses/applications")
public class ExpenseApplicationController {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ExpenseApplicationService service;

    public ExpenseApplicationController(ExpenseApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody ExpenseApplicationRequest request,
                                               @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                               Authentication authentication) {
        LoginUser user = requireLogin(authentication);
        return Result.success(createView(service.create(request, user, idempotencyKey)));
    }

    @GetMapping
    public Result<java.util.List<Map<String, Object>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        LoginUser user = requireLogin(authentication);
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page 或 size 参数无效");
        var result = service.list(user, status == null || status.isBlank() ? null : status.trim().toUpperCase(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime").and(Sort.by(Sort.Direction.DESC, "id"))));
        return Result.success(result.getContent().stream().map(this::view).toList(), page, result.getTotalElements());
    }

    private LoginUser requireLogin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)) {
            throw new ForbiddenBusinessException("请先登录");
        }
        return user;
    }

    private Map<String, Object> createView(ExpenseApplication application) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", application.getId()); view.put("status", application.getStatus());
        view.put("manualReview", application.isManualReview());
        view.put("createTime", application.getCreateTime().format(TIME));
        return view;
    }

    private Map<String, Object> view(ExpenseApplication application) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", application.getId()); view.put("orderId", application.getOrderId());
        view.put("orderTitle", application.getOrderTitle()); view.put("type", application.getType());
        view.put("amount", application.getAmount()); view.put("reason", application.getReason());
        view.put("attachments", application.getAttachments() == null ? java.util.List.of()
                : JSON.parseArray(application.getAttachments(), String.class));
        view.put("status", application.getStatus()); view.put("rejectReason", application.getRejectReason());
        view.put("manualReview", application.isManualReview()); view.put("createTime", application.getCreateTime());
        view.put("auditTime", application.getAuditTime()); view.put("paidTime", application.getPaidTime());
        return view;
    }
}

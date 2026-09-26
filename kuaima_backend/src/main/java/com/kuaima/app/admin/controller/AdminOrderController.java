package com.kuaima.app.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.Authentication;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuaima.app.common.Result;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

/**
 * 后台订单管理（基于报名记录的 admin 视角订单列表）
 */
@RestController
@RequestMapping("/admin/orders")
@Tag(name = "后台-用工订单", description = "用工订单管理")
public class AdminOrderController {

    private final BaseOrderItemRespository itemRepository;
    private final BossOrderRespository orderRepository;
    private final UserRepository userRepository;

    public AdminOrderController(BaseOrderItemRespository itemRepository,
                                BossOrderRespository orderRepository,
                                UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /** 订单/报名列表（admin 全量视图，关联雇主名称、零工名称、工种、金额、时间） */
    @Operation(summary = "全部报名记录列表", description = "admin 全量视图，支持按 status 过滤 + keyword 搜索（雇主/零工/订单号）+ 分页，按 id 倒序。返回 enriched JSON")
    @GetMapping
    public Result<Page<JSONObject>> list(@RequestParam(required = false) String status,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<BaseOrderItem> items = (status != null && !status.isBlank())
                ? itemRepository.findByStatus(status, pageable)
                : itemRepository.findAll(pageable);

        // 批量查询关联的 BossOrder（获取雇主、工种、金额、时间）
        Set<Long> orderIds = items.stream().map(BaseOrderItem::getOrderId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Map<Long, BossOrder> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            orderRepository.findAllById(orderIds).forEach(o -> orderMap.put(o.getId(), o));
        }

        // 批量查询关联的 User（零工名称、雇主名称）
        Set<Long> userIds = items.stream().map(BaseOrderItem::getUserId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Set<Long> employerIds = orderMap.values().stream().map(BossOrder::getCreateBy).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        userIds.addAll(employerIds);
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        Page<JSONObject> views = items.map(item -> {
            JSONObject obj = (JSONObject) JSON.toJSON(item);

            // 关联招工订单信息
            BossOrder order = orderMap.get(item.getOrderId());
            if (order != null) {
                obj.put("jobTitle", order.getOrderTitle());
                obj.put("jobType", order.getType());
                obj.put("postion", order.getPostion());
                obj.put("amount", order.getSalary());
                obj.put("startTime", order.getStartTime());
                obj.put("endTime", order.getEndTime());
                obj.put("orderNum", order.getOrderNum());

                // 雇主名称
                User employer = userMap.get(order.getCreateBy());
                if (employer != null) {
                    obj.put("employerName",
                            (employer.getCompanyName() != null && !employer.getCompanyName().isBlank())
                                    ? employer.getCompanyName()
                                    : (employer.getNickname() != null ? employer.getNickname() : employer.getUsername()));
                } else {
                    obj.put("employerName", "未知雇主");
                }
            }

            // 零工名称
            User worker = userMap.get(item.getUserId());
            if (worker != null) {
                obj.put("workerName",
                        (worker.getNickname() != null && !worker.getNickname().isBlank())
                                ? worker.getNickname()
                                : worker.getUsername());
            } else {
                obj.put("workerName", "未知零工");
            }

            return obj;
        });

        return Result.success(views, page, items.getTotalElements());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "后台取消订单", description = "取消指定报名记录，并记录取消原因")
    public Result<JSONObject> cancel(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String, Object> body,
                                     Authentication authentication) {
        requireAdmin(authentication);
        BaseOrderItem item = itemRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("订单不存在: " + id));
        if ("已完成".equals(item.getStatus()) || "已结算".equals(item.getStatus())) {
            throw new IllegalArgumentException("已完成或已结算订单不可取消");
        }
        item.setStatus("取消招工");
        item.setCancelReason(body == null || body.get("reason") == null ? "管理员取消" : String.valueOf(body.get("reason")));
        item.setCancelDate(new java.sql.Date(System.currentTimeMillis()));
        BaseOrderItem saved = itemRepository.save(item);
        return Result.success((JSONObject) JSON.toJSON(saved));
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)
                || user.role() == null || !user.role().startsWith("ADMIN_")) {
            throw new ForbiddenBusinessException("仅管理员可操作");
        }
    }
}

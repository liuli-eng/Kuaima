package com.kuaima.app.admin.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;

/**
 * 后台订单管理（基于报名记录的 admin 视角订单列表）
 */
@RestController
@RequestMapping("/admin/orders")
@Tag(name = "后台-用工订单", description = "用工订单管理")
public class AdminOrderController {

    private final BaseOrderItemRespository itemRepository;
    private final BossOrderRespository orderRepository;

    public AdminOrderController(BaseOrderItemRespository itemRepository,
                                BossOrderRespository orderRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    /** 订单/报名列表（admin 全量视图，支持按状态过滤 + 分页） */
    @Operation(summary = "全部报名记录列表", description = "admin 全量视图，支持按 status 过滤（已报名/已录用/已到岗/已完成/取消报名/取消招工）+ 分页，按 id 倒序")
    @GetMapping
    public Result<List<BaseOrderItem>> list(@RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<BaseOrderItem> items = (status != null && !status.isBlank())
                ? itemRepository.findByStatus(status, pageable)
                : itemRepository.findAll(pageable);
        return Result.success(items.getContent(), page, items.getTotalElements());
    }
}

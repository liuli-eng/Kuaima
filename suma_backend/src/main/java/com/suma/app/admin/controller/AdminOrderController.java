package com.suma.app.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.common.Result;
import com.suma.app.domain.boss.repository.BaseOrderItemRespository;
import com.suma.app.domain.boss.repository.BossOrderRespository;

/**
 * 后台订单管理（基于报名记录的 admin 视角订单列表）
 */
@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final BaseOrderItemRespository itemRepository;
    private final BossOrderRespository orderRepository;

    public AdminOrderController(BaseOrderItemRespository itemRepository,
                                BossOrderRespository orderRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    /** 订单/报名列表（admin 全量视图） */
    @GetMapping
    public Result<List<?>> list(@RequestParam(required = false) String status) {
        // 返回报名记录列表，admin 端展示完整订单+零工信息
        List<com.suma.app.domain.boss.entity.BaseOrderItem> items = itemRepository.findAll();
        if (status != null && !status.isBlank()) {
            items = items.stream().filter(i -> status.equals(i.getStatus())).toList();
        }
        return Result.success(items);
    }
}

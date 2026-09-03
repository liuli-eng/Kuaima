package com.suma.app.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.admin.dto.BossOrderView;
import com.suma.app.common.Result;
import com.suma.app.domain.boss.entity.BossOrder;
import com.suma.app.domain.boss.repository.BossOrderRespository;
import com.suma.app.domain.boss.service.BossOrderService;
import com.suma.app.domain.user.entity.User;
import com.suma.app.domain.user.repository.UserRepository;

/**
 * 后台招工管理 + 招工审核
 */
@RestController
@RequestMapping("/admin/jobs")
public class AdminJobController {

    private final BossOrderRespository orderRepository;
    private final BossOrderService bossOrderService;
    private final UserRepository userRepository;

    public AdminJobController(BossOrderRespository orderRepository,
                              BossOrderService bossOrderService,
                              UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.bossOrderService = bossOrderService;
        this.userRepository = userRepository;
    }

    /** 招工列表（全部状态），批量填充雇主名称 */
    @GetMapping
    public Result<Page<BossOrderView>> list(@RequestParam(required = false) String type,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<BossOrder> orders = orderRepository.search(type, status, title, pageable);

        // 批量查询雇主名称（createBy = 雇主 userId）
        Set<Long> employerIds = orders.stream()
                .map(BossOrder::getCreateBy)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Map<Long, String> employerNames = new HashMap<>();
        if (!employerIds.isEmpty()) {
            userRepository.findAllById(employerIds).forEach(u -> {
                String name = (u.getCompanyName() != null && !u.getCompanyName().isBlank())
                        ? u.getCompanyName()
                        : (u.getNickname() != null ? u.getNickname() : u.getUsername());
                employerNames.put(u.getId(), name);
            });
        }

        Page<BossOrderView> views = orders.map(order -> new BossOrderView(
                order.getId(),
                order.getOrderTitle(),
                order.getOrderContent(),
                null,
                order.getOrderNum(),
                order.getOrderStatus(),
                order.getType(),
                order.getPostion(),
                order.getDuration(),
                order.getSalary(),
                order.getAddress(),
                order.getTags(),
                order.getTrialDuration(),
                order.getTimestamp(),
                order.getStartTime(),
                order.getEndTime(),
                order.getCreateBy() != null ? employerNames.getOrDefault(order.getCreateBy(), "未知雇主") : "未知雇主"
        ));

        return Result.success(views, page, orders.getTotalElements());
    }

    /** 招工详情 */
    @GetMapping("/{id}")
    public Result<BossOrder> get(@PathVariable Long id) {
        return Result.success(orderRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("订单不存在: " + id)));
    }

    /** 审核通过 */
    @PutMapping("/{id}/audit/pass")
    public Result<BossOrder> pass(@PathVariable Long id) {
        return Result.success(bossOrderService.auditPass(id));
    }

    /** 审核拒绝 */
    @PutMapping("/{id}/audit/reject")
    public Result<BossOrder> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return Result.success(bossOrderService.auditReject(id, reason));
    }

    /** 订单状态流转（admin 端也可手动推进） */
    @PutMapping("/{id}/status")
    public Result<BossOrder> changeStatus(@PathVariable Long id, @RequestParam String target) {
        return Result.success(bossOrderService.changeOrderStatus(id, target));
    }
}

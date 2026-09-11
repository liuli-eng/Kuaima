package com.kuaima.app.admin.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import com.kuaima.app.domain.wallet.service.SettlementService;

/**
 * 后台结算管理（列出全部结算单、手动触发结算、批量结算）
 */
@RestController
@RequestMapping("/admin/settlements")
@Tag(name = "后台-结算", description = "结算审批与管理")
public class AdminSettlementController {

    private final SettlementRespository settlementRepository;
    private final SettlementService settlementService;
    private final UserRepository userRepository;
    private final BossOrderRespository orderRepository;

    public AdminSettlementController(SettlementRespository settlementRepository,
                                    SettlementService settlementService,
                                    UserRepository userRepository,
                                    BossOrderRespository orderRepository) {
        this.settlementRepository = settlementRepository;
        this.settlementService = settlementService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    /** 结算单列表（关联雇主名、零工名、订单信息） */
    @Operation(summary = "结算单列表分页", description = "按 status(待支付/已支付/已取消) 可选过滤，page/size 分页，按 id 倒序。附加 employerName/workerName/jobTitle/amount 等关联字段")
    @GetMapping
    public Result<Page<JSONObject>> list(@RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Settlement> result = settlementRepository.findAll(pageable);

        // 批量查询关联订单
        Set<Long> orderIds = result.stream().map(Settlement::getOrderId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Map<Long, BossOrder> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            orderRepository.findAllById(orderIds).forEach(o -> orderMap.put(o.getId(), o));
        }

        // 批量查询关联用户（零工 + 雇主）
        Set<Long> userIds = result.stream().map(Settlement::getWorkerId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Set<Long> employerIds = orderMap.values().stream().map(BossOrder::getCreateBy).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        userIds.addAll(employerIds);
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        Page<JSONObject> views = result.map(s -> {
            JSONObject obj = (JSONObject) JSON.toJSON(s);

            // 金额转换：分 → 元
            obj.put("amount", s.getWage() != null ? s.getWage() / 100.0 : 0);
            obj.put("platformFee", s.getServiceFee() != null ? s.getServiceFee() / 100.0 : 0);
            obj.put("actualAmount", s.getTotalAmount() != null ? s.getTotalAmount() / 100.0 : 0);

            // 状态映射
            obj.put("status", mapStatus(s.getStatus()));
            obj.put("time", s.getPayTime() != null ? s.getPayTime().toString() : (s.getTimestamp() != null ? s.getTimestamp().toString() : null));

            // 关联订单信息
            BossOrder order = orderMap.get(s.getOrderId());
            if (order != null) {
                obj.put("jobTitle", order.getOrderTitle());
                obj.put("postion", order.getPostion());
                obj.put("orderNum", order.getOrderNum());
                User employer = userMap.get(order.getCreateBy());
                if (employer != null) {
                    obj.put("employerName",
                            (employer.getCompanyName() != null && !employer.getCompanyName().isBlank())
                                    ? employer.getCompanyName()
                                    : (employer.getNickname() != null ? employer.getNickname() : employer.getUsername()));
                }
            }

            // 零工名称
            User worker = userMap.get(s.getWorkerId());
            if (worker != null) {
                obj.put("workerName",
                        (worker.getNickname() != null && !worker.getNickname().isBlank())
                                ? worker.getNickname()
                                : worker.getUsername());
            }

            return obj;
        });

        return Result.success(views, page, result.getTotalElements());
    }

    /** 后端状态 → 前端展示状态 */
    private String mapStatus(String s) {
        if (s == null) return null;
        return switch (s) {
            case "待支付" -> "待结算";
            case "已支付" -> "已结算";
            case "已取消" -> "结算失败";
            default -> s;
        };
    }

    /** 结算单详情 */
    @Operation(summary = "结算单详情", description = "按 id 查询 Settlement 完整信息，附加雇主/零工/订单名称")
    @GetMapping("/{id}")
    public Result<JSONObject> get(@PathVariable Long id) {
        Settlement s = settlementRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("结算单不存在: " + id));
        JSONObject obj = (JSONObject) JSON.toJSON(s);
        obj.put("amount", s.getWage() != null ? s.getWage() / 100.0 : 0);
        obj.put("platformFee", s.getServiceFee() != null ? s.getServiceFee() / 100.0 : 0);
        obj.put("actualAmount", s.getTotalAmount() != null ? s.getTotalAmount() / 100.0 : 0);
        obj.put("status", mapStatus(s.getStatus()));

        // 关联订单
        if (s.getOrderId() != null) {
            orderRepository.findById(s.getOrderId()).ifPresent(order -> {
                obj.put("jobTitle", order.getOrderTitle());
                obj.put("postion", order.getPostion());
                obj.put("orderNum", order.getOrderNum());
                if (order.getCreateBy() != null) {
                    userRepository.findById(order.getCreateBy()).ifPresent(employer -> {
                        obj.put("employerName",
                                (employer.getCompanyName() != null && !employer.getCompanyName().isBlank())
                                        ? employer.getCompanyName()
                                        : (employer.getNickname() != null ? employer.getNickname() : employer.getUsername()));
                        obj.put("employerPhone", employer.getPhone());
                    });
                }
            });
        }
        // 零工
        if (s.getWorkerId() != null) {
            userRepository.findById(s.getWorkerId()).ifPresent(worker -> {
                obj.put("workerName",
                        (worker.getNickname() != null && !worker.getNickname().isBlank())
                                ? worker.getNickname()
                                : worker.getUsername());
                obj.put("workerPhone", worker.getPhone());
            });
        }

        return Result.success(obj);
    }

    /** admin 手动结算（模拟支付） */
    @Operation(summary = "手动结算", description = "模拟支付：「待支付 → 已支付」，工资入零工钱包。仅待支付的结算单可以支付")
    @PostMapping("/{id}/pay")
    public Result<Settlement> pay(@PathVariable Long id) {
        return Result.success(settlementService.mockPay(id));
    }
}

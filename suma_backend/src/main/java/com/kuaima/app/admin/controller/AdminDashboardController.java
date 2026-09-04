package com.kuaima.app.admin.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.dto.BossOrderView;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

/**
 * 后台 Dashboard 聚合统计接口
 */
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final BossOrderRespository orderRepository;
    private final SettlementRespository settlementRepository;

    public AdminDashboardController(UserRepository userRepository,
                                    BossOrderRespository orderRepository,
                                    SettlementRespository settlementRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.settlementRepository = settlementRepository;
    }

    /** 基础统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();

        long workerCount = userRepository.countByRole(UserRole.USER);
        long bossCount = userRepository.countByRole(UserRole.BOSS);
        long orderTotal = orderRepository.count();
        long settledTotal = settlementRepository.count();

        data.put("workerTotal", workerCount);
        data.put("bossTotal", bossCount);
        data.put("orderTotal", orderTotal);
        data.put("settledTotal", settledTotal);

        // 待审核订单数
        long pendingAudit = orderRepository.findAll().stream()
                .filter(o -> "待审核".equals(o.getOrderStatus())).count();
        data.put("pendingAudit", pendingAudit);

        return Result.success(data);
    }

    /** 订单趋势（近7天每日订单数） */
    @GetMapping("/trend")
    public Result<Map<String, Object>> trend() {
        List<BossOrder> allOrders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        // 按日期分组统计近7天
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        Map<String, Long> dailyCount = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DAY_OF_MONTH, i);
            dailyCount.put(sdf.format(c.getTime()), 0L);
        }

        for (BossOrder order : allOrders) {
            Timestamp ts = order.getTimestamp();
            if (ts != null && !ts.before(startDate)) {
                String key = sdf.format(ts);
                dailyCount.merge(key, 1L, Long::sum);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("labels", new ArrayList<>(dailyCount.keySet()));
        data.put("values", new ArrayList<>(dailyCount.values()));
        return Result.success(data);
    }

    /** 工种分布（按 type 字段分组统计） */
    @GetMapping("/distribution")
    public Result<List<Map<String, Object>>> distribution() {
        List<BossOrder> allOrders = orderRepository.findAll();

        Map<String, Long> typeCount = allOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> {
                            String t = o.getType();
                            if (t == null || t.isBlank()) return "其他";
                            return switch (t) {
                                case "daily" -> "日结";
                                case "heldBack" -> "压薪日结";
                                case "month" -> "月结";
                                default -> t;
                            };
                        },
                        Collectors.counting()
                ));

        String[] colors = {"#FF6B35", "#2563EB", "#10B981", "#F59E0B", "#8B5CF6"};
        List<Map<String, Object>> result = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            item.put("color", colors[i % colors.length]);
            result.add(item);
            i++;
        }
        return Result.success(result);
    }

    /** 最近订单（最新8条，含雇主名称） */
    @GetMapping("/recent-orders")
    public Result<List<BossOrderView>> recentOrders() {
        PageRequest pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id"));
        Page<BossOrder> orders = orderRepository.findAll(pageable);

        // 批量查询雇主名称
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

        List<BossOrderView> views = orders.getContent().stream()
                .map(order -> new BossOrderView(
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
                ))
                .collect(Collectors.toList());

        return Result.success(views);
    }
}

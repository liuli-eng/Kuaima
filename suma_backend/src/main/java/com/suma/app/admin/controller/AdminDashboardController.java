package com.suma.app.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.common.Result;
import com.suma.app.domain.boss.repository.BossOrderRespository;
import com.suma.app.domain.user.constant.UserRole;
import com.suma.app.domain.user.repository.UserRepository;
import com.suma.app.domain.wallet.repository.SettlementRespository;

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
}

package com.kuaima.app.controller.finance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.points.entity.PointsAccount;
import com.kuaima.app.domain.points.entity.PointsFlow;
import com.kuaima.app.domain.points.repository.PointsAccountRepository;
import com.kuaima.app.domain.points.repository.PointsFlowRepository;

/**
 * 积分账户与流水。
 */
@RestController
@RequestMapping("/points")
public class PointsController {

    private final PointsAccountRepository accountRepository;
    private final PointsFlowRepository flowRepository;

    public PointsController(PointsAccountRepository accountRepository, PointsFlowRepository flowRepository) {
        this.accountRepository = accountRepository;
        this.flowRepository = flowRepository;
    }

    /** 积分余额：GET /points/{userId} */
    @GetMapping("/{userId}")
    public Result<Map<String, Object>> getBalance(@PathVariable Long userId) {
        PointsAccount account = accountRepository.findByUserId(userId).orElseGet(() -> {
            PointsAccount a = new PointsAccount();
            a.setUserId(userId);
            a.setBalance(0);
            return accountRepository.save(a);
        });
        Map<String, Object> data = new HashMap<>();
        data.put("balance", account.getBalance());
        return Result.success(data);
    }

    /** 积分明细分页：GET /points/{userId}/flows?page=0&size=20 */
    @GetMapping("/{userId}/flows")
    public Result<List<PointsFlow>> listFlows(@PathVariable Long userId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<PointsFlow> result = flowRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }
}

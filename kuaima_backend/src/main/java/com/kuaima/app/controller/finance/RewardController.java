package com.kuaima.app.controller.finance;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.points.entity.PointsAccount;
import com.kuaima.app.domain.points.entity.PointsFlow;
import com.kuaima.app.domain.points.repository.PointsAccountRepository;
import com.kuaima.app.domain.points.repository.PointsFlowRepository;
import com.kuaima.app.domain.reward.entity.Reward;
import com.kuaima.app.domain.reward.entity.RewardExchange;
import com.kuaima.app.domain.reward.repository.RewardExchangeRepository;
import com.kuaima.app.domain.reward.repository.RewardRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 积分奖励兑换。
 */
@RestController
@RequestMapping("/rewards")
public class RewardController {

    private final RewardRepository rewardRepository;
    private final RewardExchangeRepository exchangeRepository;
    private final PointsAccountRepository pointsAccountRepository;
    private final PointsFlowRepository pointsFlowRepository;

    public RewardController(RewardRepository rewardRepository,
                            RewardExchangeRepository exchangeRepository,
                            PointsAccountRepository pointsAccountRepository,
                            PointsFlowRepository pointsFlowRepository) {
        this.rewardRepository = rewardRepository;
        this.exchangeRepository = exchangeRepository;
        this.pointsAccountRepository = pointsAccountRepository;
        this.pointsFlowRepository = pointsFlowRepository;
    }

    /** 奖励列表：GET /rewards */
    @GetMapping
    public Result<List<Reward>> listRewards() {
        return Result.success(rewardRepository.findAll());
    }

    /** 奖励详情：GET /rewards/{id} */
    @GetMapping("/{id}")
    public Result<Reward> getReward(@PathVariable Long id) {
        return Result.success(rewardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("奖品不存在: " + id)));
    }

    /** 积分兑换：POST /rewards/{id}/exchange?userId=1 */
    @PostMapping("/{id}/exchange")
    @Transactional
    public Result<RewardExchange> exchange(@PathVariable Long id, @RequestParam Long userId) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("奖品不存在: " + id));
        if (reward.getStock() != null && reward.getStock() <= 0) {
            throw new IllegalStateException("奖品库存不足");
        }
        // 扣减积分
        PointsAccount account = pointsAccountRepository.findByUserId(userId).orElseGet(() -> {
            PointsAccount a = new PointsAccount();
            a.setUserId(userId);
            a.setBalance(0);
            return a;
        });
        int cost = reward.getPointsCost() != null ? reward.getPointsCost() : 0;
        if ((account.getBalance() == null ? 0 : account.getBalance()) < cost) {
            throw new IllegalStateException("积分余额不足");
        }
        account.setBalance(account.getBalance() - cost);
        pointsAccountRepository.save(account);
        // 记录积分流水
        PointsFlow flow = new PointsFlow();
        flow.setUserId(userId);
        flow.setDelta(-cost);
        flow.setBizType("REWARD_EXCHANGE");
        flow.setRemark("兑换奖品: " + reward.getTitle());
        pointsFlowRepository.save(flow);
        // 扣减库存
        if (reward.getStock() != null) {
            reward.setStock(reward.getStock() - 1);
            rewardRepository.save(reward);
        }
        // 创建兑换记录
        RewardExchange exchange = new RewardExchange();
        exchange.setUserId(userId);
        exchange.setRewardId(id);
        exchange.setStatus("PENDING");
        return Result.success(exchangeRepository.save(exchange));
    }

    /** 兑换记录：GET /rewards/exchanges?userId=1 */
    @GetMapping("/exchanges")
    public Result<List<RewardExchange>> listExchanges(@RequestParam Long userId) {
        return Result.success(exchangeRepository.findByUserId(userId));
    }
}

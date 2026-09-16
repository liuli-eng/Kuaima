package com.kuaima.app.domain.user.service;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.points.repository.PointsAccountRepository;
import com.kuaima.app.domain.starlevel.repository.UserStarLevelRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.model.WorkerProfileOverview;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import com.kuaima.app.domain.wallet.repository.WalletFlowRespository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkerProfileOverviewService {
    private final UserRepository users;
    private final BaseOrderItemRespository items;
    private final SettlementRespository settlements;
    private final PointsAccountRepository points;
    private final UserStarLevelRepository starLevels;
    private final WalletFlowRespository walletFlows;

    public WorkerProfileOverviewService(UserRepository users, BaseOrderItemRespository items,
            SettlementRespository settlements, PointsAccountRepository points,
            UserStarLevelRepository starLevels, WalletFlowRespository walletFlows) {
        this.users = users; this.items = items; this.settlements = settlements;
        this.points = points; this.starLevels = starLevels; this.walletFlows = walletFlows;
    }

    @Transactional(readOnly = true)
    public WorkerProfileOverview overview(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> new EntityNotFoundException("零工不存在: " + userId));
        long completedOrders = items.countByUserIdAndStatus(userId, BossStatus.ITEM_FINISHED);
        long earlyLeaves = items.countByUserIdAndStatusAndEarlyLeaveTrue(userId, BossStatus.ITEM_FINISHED);
        long normalCompletions = Math.max(0, completedOrders - earlyLeaves);
        long cancellations = items.countByUserIdAndStatus(userId, BossStatus.ITEM_CANCELED);
        long noShows = items.countNoShowByUserId(userId, new Date());
        long performanceTotal = normalCompletions + cancellations + noShows + earlyLeaves;

        int level = starLevels.findByUserId(userId).map(v -> value(v.getLevel(), 1)).orElse(1);
        int creditScore = clamp(value(user.getCreditScore(), 0));
        int pointBalance = points.findByUserId(userId).map(v -> value(v.getBalance(), 0)).orElse(0);
        long totalIncome = nullableLong(settlements.sumPaidWageByWorkerId(userId));
        long rewardAmount = nullableLong(walletFlows.sumIncomeByUserIdAndBizType(userId, "REWARD"));
        return new WorkerProfileOverview(level, creditScore,
                rate(normalCompletions, performanceTotal), rate(cancellations, performanceTotal),
                rate(noShows, performanceTotal), rate(earlyLeaves, performanceTotal),
                totalIncome, completedOrders, pointBalance, rewardAmount);
    }

    private int rate(long count, long total) { return total == 0 ? 0 : clamp((int) Math.round(count * 100.0 / total)); }
    private int clamp(int value) { return Math.max(0, Math.min(100, value)); }
    private int value(Integer value, int fallback) { return value == null ? fallback : value; }
    private long nullableLong(Long value) { return value == null ? 0L : value; }
}

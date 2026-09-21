package com.kuaima.app.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.points.entity.PointsAccount;
import com.kuaima.app.domain.points.repository.PointsAccountRepository;
import com.kuaima.app.domain.reward.entity.RewardAccount;
import com.kuaima.app.domain.reward.repository.RewardAccountRepository;
import com.kuaima.app.domain.starlevel.entity.UserStarLevel;
import com.kuaima.app.domain.starlevel.repository.UserStarLevelRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WorkerProfileOverviewServiceTests {
    @Test
    void aggregatesRealBalancesOrdersAndPerformanceRates() {
        UserRepository users = mock(UserRepository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        PointsAccountRepository points = mock(PointsAccountRepository.class);
        UserStarLevelRepository levels = mock(UserStarLevelRepository.class);
        RewardAccountRepository rewardAccounts = mock(RewardAccountRepository.class);
        WorkerProfileOverviewService service = new WorkerProfileOverviewService(
                users, items, settlements, points, levels, rewardAccounts);
        User user = new User(); user.setId(30L); user.setCreditScore(88);
        PointsAccount account = new PointsAccount(); account.setBalance(500);
        UserStarLevel level = new UserStarLevel(); level.setLevel(3);
        when(users.findById(30L)).thenReturn(Optional.of(user));
        when(items.countByUserId(30L)).thenReturn(10L);
        when(items.countByUserIdAndStatus(30L, BossStatus.ITEM_FINISHED)).thenReturn(8L);
        when(items.countByUserIdAndStatusAndEarlyLeaveTrue(30L, BossStatus.ITEM_FINISHED)).thenReturn(1L);
        when(items.countByUserIdAndStatus(30L, BossStatus.ITEM_CANCELED)).thenReturn(1L);
        when(items.countByUserIdAndHireDateIsNotNull(30L)).thenReturn(10L);
        when(items.countNoShowByUserId(eq(30L), any())).thenReturn(1L);
        when(settlements.sumWorkDaysByWorkerId(30L)).thenReturn(20L);
        when(settlements.sumEarlyLeaveWorkDaysByWorkerId(30L)).thenReturn(2L);
        when(settlements.sumPaidWageByWorkerId(30L)).thenReturn(123400L);
        when(points.findByUserIdAndRole(30L, com.kuaima.app.domain.user.constant.UserRole.USER))
                .thenReturn(Optional.of(account));
        when(levels.findByUserId(30L)).thenReturn(Optional.of(level));
        RewardAccount rewardAccount = new RewardAccount();
        rewardAccount.setBalance(new java.math.BigDecimal("50.00"));
        when(rewardAccounts.findByUserId(30L)).thenReturn(Optional.of(rewardAccount));

        var result = service.overview(30L);

        assertEquals(3, result.level()); assertEquals(88, result.creditScore());
        assertEquals(70, result.completionRate()); assertEquals(10, result.cancellationRate());
        assertEquals(10, result.noShowRate()); assertEquals(10, result.earlyLeaveRate());
        assertEquals(123400L, result.totalIncome()); assertEquals(7L, result.completedOrders());
        assertEquals(500, result.points()); assertEquals(5000L, result.rewardAmount());
    }

    @Test
    void emptyAccountsAndHistoryReturnNumericDefaults() {
        UserRepository users = mock(UserRepository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        PointsAccountRepository points = mock(PointsAccountRepository.class);
        UserStarLevelRepository levels = mock(UserStarLevelRepository.class);
        RewardAccountRepository rewardAccounts = mock(RewardAccountRepository.class);
        WorkerProfileOverviewService service = new WorkerProfileOverviewService(
                users, items, settlements, points, levels, rewardAccounts);
        User user = new User(); user.setId(30L);
        when(users.findById(30L)).thenReturn(Optional.of(user));

        var result = service.overview(30L);

        assertEquals(1, result.level()); assertEquals(0, result.creditScore());
        assertEquals(0, result.completionRate()); assertEquals(0, result.totalIncome());
        assertEquals(0, result.points()); assertEquals(0, result.rewardAmount());
    }
}

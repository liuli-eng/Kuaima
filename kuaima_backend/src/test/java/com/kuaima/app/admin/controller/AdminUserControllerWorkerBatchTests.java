package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.coupon.repository.CouponRepository;
import com.kuaima.app.domain.coupon.repository.UserCouponRepository;
import com.kuaima.app.domain.points.entity.PointsAccount;
import com.kuaima.app.domain.points.repository.PointsAccountRepository;
import com.kuaima.app.domain.points.repository.PointsFlowRepository;
import com.kuaima.app.domain.reward.entity.RewardAccount;
import com.kuaima.app.domain.reward.repository.RewardAccountRepository;
import com.kuaima.app.domain.reward.repository.RewardFlowRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.WalletFlowRespository;
import com.kuaima.app.domain.wallet.repository.WalletRespository;
import com.kuaima.app.security.model.LoginUser;

class AdminUserControllerWorkerBatchTests {

    @Test
    void workersLoadsPageStatisticsInBatches() {
        UserRepository users = mock(UserRepository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        PointsAccountRepository points = mock(PointsAccountRepository.class);
        RewardAccountRepository rewards = mock(RewardAccountRepository.class);
        WalletFlowRespository walletFlows = mock(WalletFlowRespository.class);
        User first = worker(10L); User second = worker(11L);
        BaseOrderItem completed = new BaseOrderItem(); completed.setUserId(10L); completed.setStatus("已完成");
        BaseOrderItem canceled = new BaseOrderItem(); canceled.setUserId(11L); canceled.setStatus("已取消");
        RewardAccount reward = new RewardAccount(); reward.setUserId(10L); reward.setRole("USER"); reward.setBalance(new BigDecimal("12.50"));
        PointsAccount point = new PointsAccount(); point.setUserId(11L); point.setRole("USER"); point.setBalance(80);
        when(users.searchWorkers(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(first, second)));
        when(items.countCompletedByUserIds(any())).thenReturn(Arrays.<Object[]>asList(new Object[]{10L, 1L}));
        when(items.findByUserIdIn(any())).thenReturn(List.of(completed, canceled));
        when(walletFlows.sumIncomeByUserIds(any())).thenReturn(Arrays.<Object[]>asList(new Object[]{10L, 500L}));
        when(rewards.findByUserIdIn(any())).thenReturn(List.of(reward));
        when(points.findByUserIdInAndRole(any(), any())).thenReturn(List.of(point));
        AdminUserController controller = new AdminUserController(users, items, mock(BossOrderRespository.class),
                mock(WalletRespository.class), points, rewards, mock(PointsFlowRepository.class),
                mock(RewardFlowRepository.class), mock(UserCouponRepository.class), mock(CouponRepository.class),
                walletFlows, null);

        var rows = controller.workers(null, null, null, null, 0, 10, admin()).getData().getContent();

        assertEquals(1L, rows.get(0).get("completedOrders"));
        assertEquals(86, rows.get(0).get("creditScore"));
        assertEquals(500L, rows.get(0).get("totalIncome"));
        assertEquals(new BigDecimal("12.50"), rows.get(0).get("rewardBalance"));
        assertEquals(80L, rows.get(1).get("pointsBalance"));
        verify(items).findByUserIdIn(any());
        verify(walletFlows).sumIncomeByUserIds(any());
        verify(items, never()).findByUserId(any());
        verify(walletFlows, never()).sumIncomeByUserId(any());
        verify(rewards, never()).findFirstByUserIdAndRoleOrderByIdDesc(any(), any());
        verify(points, never()).findFirstByUserIdAndRoleOrderByIdDesc(any(), any());
    }

    private User worker(Long id) {
        User user = new User(); user.setId(id); user.setRole("USER"); user.setUsername("worker-" + id);
        user.setCreditScore(12); user.setStarScore(id.equals(10L) ? 86 : 72);
        user.setPassword("hidden"); return user;
    }

    private UsernamePasswordAuthenticationToken admin() {
        return new UsernamePasswordAuthenticationToken(new LoginUser(1L, "admin", "ADMIN_ADMIN"), null);
    }
}

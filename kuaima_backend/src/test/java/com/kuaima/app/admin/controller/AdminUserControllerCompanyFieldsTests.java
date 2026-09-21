package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.coupon.repository.CouponRepository;
import com.kuaima.app.domain.coupon.entity.Coupon;
import com.kuaima.app.domain.coupon.entity.UserCoupon;
import com.kuaima.app.domain.coupon.repository.UserCouponRepository;
import com.kuaima.app.domain.points.entity.PointsAccount;
import com.kuaima.app.domain.points.entity.PointsFlow;
import com.kuaima.app.domain.points.repository.PointsAccountRepository;
import com.kuaima.app.domain.points.repository.PointsFlowRepository;
import com.kuaima.app.domain.reward.entity.RewardAccount;
import com.kuaima.app.domain.reward.entity.RewardFlow;
import com.kuaima.app.domain.reward.repository.RewardAccountRepository;
import com.kuaima.app.domain.reward.repository.RewardFlowRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.entity.Wallet;
import com.kuaima.app.domain.wallet.repository.WalletRespository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import com.kuaima.app.common.ForbiddenBusinessException;

class AdminUserControllerCompanyFieldsTests {
    @Test
    void bossesAlwaysReturnsCompanyFieldsEvenWhenUnverified() {
        UserRepository users = mock(UserRepository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        BossOrderRespository orders = mock(BossOrderRespository.class);
        WalletRespository wallets = mock(WalletRespository.class);
        PointsAccountRepository pointsRepository = mock(PointsAccountRepository.class);
        RewardAccountRepository rewards = mock(RewardAccountRepository.class);
        PointsFlowRepository pointFlows = mock(PointsFlowRepository.class);
        RewardFlowRepository rewardFlows = mock(RewardFlowRepository.class);
        UserCouponRepository userCoupons = mock(UserCouponRepository.class);
        CouponRepository coupons = mock(CouponRepository.class);
        User user = new User(); user.setId(59L); user.setRole("BOSS");
        user.setCreditScore(88);
        Wallet wallet = new Wallet(); wallet.setUserId(59L); wallet.setBalance(123456L);
        PointsAccount pointsAccount = new PointsAccount(); pointsAccount.setUserId(59L); pointsAccount.setRole("BOSS"); pointsAccount.setBalance(1680);
        RewardAccount reward = new RewardAccount(); reward.setUserId(59L); reward.setBalance(7890L);
        when(users.searchBosses(any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(user)));
        when(orders.countByCreateByIds(any())).thenReturn(List.of());
        when(wallets.findByUserIdIn(any())).thenReturn(List.of(wallet));
        when(pointsRepository.findByUserIdInAndRole(any(), any())).thenReturn(List.of(pointsAccount));
        when(rewards.findByUserIdIn(any())).thenReturn(List.of(reward));
        AdminUserController controller = new AdminUserController(users, items, orders, wallets,
                pointsRepository, rewards, pointFlows, rewardFlows, userCoupons, coupons);

        var row = controller.bosses(null, null, null, null, 0, 100).getData().getContent().get(0);

        assertTrue(row.containsKey("companyCode"));
        assertTrue(row.containsKey("companyName"));
        assertNull(row.get("companyCode"));
        assertNull(row.get("companyName"));
        assertEquals(88, row.get("creditScore"));
        assertEquals(new java.math.BigDecimal("123456"), row.get("balance"));
        assertEquals(new java.math.BigDecimal("1680"), row.get("points"));
        assertEquals(new java.math.BigDecimal("7890"), row.get("rewardAmount"));
    }

    @Test
    void bossAssetEndpointsShouldReturnRoleIsolatedPointAndRewardRecords() {
        var controller = controller(mock(UserRepository.class), mock(PointsFlowRepository.class),
                mock(RewardFlowRepository.class), mock(UserCouponRepository.class), mock(CouponRepository.class));
        UserRepository users = mock(UserRepository.class);
        PointsFlowRepository pointFlows = mock(PointsFlowRepository.class);
        RewardFlowRepository rewardFlows = mock(RewardFlowRepository.class);
        User boss = new User(); boss.setId(59L); boss.setRole("BOSS");
        PointsFlow pointFlow = new PointsFlow(); pointFlow.setId(1L); pointFlow.setRole("BOSS");
        pointFlow.setBizType("PURCHASE"); pointFlow.setDelta(100); pointFlow.setBalanceAfter(100);
        RewardFlow rewardFlow = new RewardFlow(); rewardFlow.setId(2L); rewardFlow.setType("INCOME");
        rewardFlow.setAmount(500L); rewardFlow.setBalanceAfter(500L);
        when(users.findById(59L)).thenReturn(Optional.of(boss));
        when(pointFlows.findByUserIdAndRoleAndType(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(pointFlow)));
        when(rewardFlows.findByUserIdOrderByCreatedAtDescIdDesc(any(), any()))
                .thenReturn(new PageImpl<>(List.of(rewardFlow)));
        controller = controller(users, pointFlows, rewardFlows, mock(UserCouponRepository.class), mock(CouponRepository.class));

        var points = controller.pointRecords(59L, "ALL", 0, 5);
        var rewards = controller.rewardRecords(59L, "ALL", 0, 5);

        assertEquals(1, points.getData().getContent().size());
        assertEquals("PURCHASE", points.getData().getContent().get(0).get("type"));
        assertEquals(1, rewards.getData().getContent().size());
        assertEquals("INCOME", rewards.getData().getContent().get(0).get("type"));
    }

    @Test
    void couponEndpointShouldJoinCouponTemplateAndRejectNonBoss() {
        UserRepository users = mock(UserRepository.class);
        UserCouponRepository userCoupons = mock(UserCouponRepository.class);
        CouponRepository couponRepository = mock(CouponRepository.class);
        User boss = new User(); boss.setId(59L); boss.setRole("BOSS");
        UserCoupon record = new UserCoupon(); record.setId(3L); record.setUserId(59L);
        record.setCouponId(4L); record.setStatus("UNUSED");
        Coupon coupon = new Coupon(); coupon.setId(4L); coupon.setTitle("招工满减券");
        coupon.setType("FULL"); coupon.setAmount(new java.math.BigDecimal("50"));
        when(users.findById(59L)).thenReturn(Optional.of(boss));
        when(userCoupons.findAvailableByUserId(any(), any(), any())).thenReturn(new PageImpl<>(List.of(record)));
        when(couponRepository.findAllById(any())).thenReturn(List.of(coupon));
        var controller = controller(users, mock(PointsFlowRepository.class),
                mock(RewardFlowRepository.class), userCoupons, couponRepository);

        var result = controller.couponRecords(59L, "AVAILABLE", 0, 5);

        assertEquals("招工满减券", result.getData().getContent().get(0).get("title"));

        User worker = new User(); worker.setId(60L); worker.setRole("USER");
        when(users.findById(60L)).thenReturn(Optional.of(worker));
        assertThrows(ForbiddenBusinessException.class,
                () -> controller.couponRecords(60L, "ALL", 0, 5));
    }

    private AdminUserController controller(UserRepository users, PointsFlowRepository pointFlows,
                                           RewardFlowRepository rewardFlows, UserCouponRepository userCoupons,
                                           CouponRepository couponRepository) {
        return new AdminUserController(users, mock(BaseOrderItemRespository.class),
                mock(BossOrderRespository.class), mock(WalletRespository.class),
                mock(PointsAccountRepository.class), mock(RewardAccountRepository.class),
                pointFlows, rewardFlows, userCoupons, couponRepository);
    }
}

package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.coupon.repository.AdminCouponRecordRow;
import com.kuaima.app.domain.coupon.repository.CouponRepository;
import com.kuaima.app.domain.coupon.repository.UserCouponRepository;
import com.kuaima.app.domain.points.repository.PointsAccountRepository;
import com.kuaima.app.domain.points.repository.PointsFlowRepository;
import com.kuaima.app.domain.reward.repository.RewardAccountRepository;
import com.kuaima.app.domain.reward.repository.RewardFlowRepository;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.WalletRespository;

class AdminUserControllerCouponBatchTests {
    @Test
    void couponHistoryUsesSingleJoinedQuery() {
        UserCouponRepository coupons = mock(UserCouponRepository.class);
        AdminCouponRecordRow row = mock(AdminCouponRecordRow.class);
        when(row.getTotalCount()).thenReturn(1L); when(row.getId()).thenReturn(3L);
        when(row.getStatus()).thenReturn("USED"); when(row.getTitle()).thenReturn("招工券");
        when(coupons.findAdminCouponRecords(eq(64L), eq("HISTORY"), any(Date.class), eq(5), eq(0)))
                .thenReturn(List.of(row));
        AdminUserController controller = new AdminUserController(mock(UserRepository.class), mock(BaseOrderItemRespository.class),
                mock(BossOrderRespository.class), mock(WalletRespository.class), mock(PointsAccountRepository.class),
                mock(RewardAccountRepository.class), mock(PointsFlowRepository.class), mock(RewardFlowRepository.class),
                coupons, mock(CouponRepository.class));
        var result = controller.couponRecords(64L, "HISTORY", 0, 5);
        assertEquals(1L, result.getData().getTotalElements());
        assertEquals("招工券", result.getData().getContent().get(0).get("title"));
        verify(coupons).findAdminCouponRecords(eq(64L), eq("HISTORY"), any(Date.class), eq(5), eq(0));
    }
}

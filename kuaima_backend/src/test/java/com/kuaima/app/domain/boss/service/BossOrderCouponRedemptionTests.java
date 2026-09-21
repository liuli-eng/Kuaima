package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.*;
import com.kuaima.app.domain.coupon.service.BossCouponService;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

class BossOrderCouponRedemptionTests {
    @Test
    void redeemsOnlyAfterOrderHasBeenSaved() {
        BossOrderRespository orders = mock(BossOrderRespository.class);
        BossCouponService coupons = mock(BossCouponService.class);
        BossOrderService service = new BossOrderService(orders, mock(BaseOrderItemRespository.class),
                mock(UserRepository.class), mock(MessageService.class), mock(SettlementRespository.class),
                null, null, coupons);
        BossOrder order = validOrder(); order.setCreateBy(7L); order.setUserCouponId(11L);
        when(orders.save(order)).thenAnswer(invocation -> { order.setId(31L); return order; });

        service.createOrder(order);

        var sequence = inOrder(orders, coupons);
        sequence.verify(orders).save(order);
        sequence.verify(coupons).redeem(7L, 11L, order);
    }

    @Test
    void invalidOrderNeverRedeemsCoupon() {
        BossCouponService coupons = mock(BossCouponService.class);
        BossOrderService service = new BossOrderService(mock(BossOrderRespository.class), mock(BaseOrderItemRespository.class),
                mock(UserRepository.class), mock(MessageService.class), mock(SettlementRespository.class),
                null, null, coupons);
        BossOrder order = validOrder(); order.setOrderTitle(null); order.setCreateBy(7L); order.setUserCouponId(11L);
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(order));
        verifyNoInteractions(coupons);
    }

    private BossOrder validOrder() {
        BossOrder order = new BossOrder(); order.setOrderTitle("仓库分拣"); order.setType("daily");
        order.setPostion("分拣员"); order.setOrderNum(2); order.setDuration(1); order.setSalary(200);
        order.setLongitude(new BigDecimal("121.4737010")); order.setLatitude(new BigDecimal("31.2304160"));
        return order;
    }
}

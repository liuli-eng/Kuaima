package com.kuaima.app.controller.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kuaima.app.domain.coupon.service.BossCouponService;
import com.kuaima.app.domain.coupon.service.CouponClaimService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CouponControllerTests {
    @Test
    void listReturnsCouponDefinitionFieldsInsteadOfBareClaimRecords() {
        BossCouponService couponService = mock(BossCouponService.class);
        CouponController controller = new CouponController(couponService, mock(CouponClaimService.class));
        Map<String, Object> coupon = Map.of(
                "userCouponId", 11L,
                "couponId", 21L,
                "name", "新人优惠券",
                "amount", new BigDecimal("20"),
                "threshold", new BigDecimal("100"),
                "status", "UNUSED");
        when(couponService.list(1L, "UNUSED")).thenReturn(List.of(coupon));

        var result = controller.listCoupons(1L, "UNUSED");

        assertEquals("新人优惠券", result.getData().get(0).get("name"));
        assertEquals(new BigDecimal("20"), result.getData().get(0).get("amount"));
        assertEquals(new BigDecimal("100"), result.getData().get(0).get("threshold"));
        verify(couponService).list(1L, "UNUSED");
    }
}

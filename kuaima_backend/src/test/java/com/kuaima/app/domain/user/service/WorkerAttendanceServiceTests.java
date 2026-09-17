package com.kuaima.app.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.service.BossAttendanceCodeService;
import com.kuaima.app.domain.boss.service.BossOrderService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WorkerAttendanceServiceTests {
    @Test
    void earlyLeavePersistsPerformanceMarker() {
        BossOrderRespository orders = mock(BossOrderRespository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        BossAttendanceCodeService codes = mock(BossAttendanceCodeService.class);
        BossOrderService bossOrders = mock(BossOrderService.class);
        WorkerAttendanceService service = new WorkerAttendanceService(orders, items, codes, bossOrders);
        BossOrder order = new BossOrder(); order.setId(10L); order.setCreateBy(20L);
        BaseOrderItem item = new BaseOrderItem(); item.setId(40L); item.setOrderId(10L); item.setUserId(30L);
        item.setStatus(BossStatus.ITEM_ON_WORK);
        BossOrder updated = new BossOrder(); updated.setId(10L); updated.setOrderStatus(BossStatus.ORDER_PENDING_SETTLE);
        when(orders.findById(10L)).thenReturn(Optional.of(order));
        when(items.findByOrderIdAndUserId(10L, 30L)).thenReturn(Optional.of(item));
        when(bossOrders.finishByEarlyLeave(40L)).thenReturn(updated);

        var result = service.earlyLeave(30L, 10L, "1234");

        assertEquals(BossStatus.ORDER_PENDING_SETTLE, result.get("status"));
        assertEquals(BossStatus.ITEM_PENDING_SETTLE, result.get("itemStatus"));
        verify(codes).verify(20L, false, "1234");
        verify(bossOrders).finishByEarlyLeave(40L);
    }
}

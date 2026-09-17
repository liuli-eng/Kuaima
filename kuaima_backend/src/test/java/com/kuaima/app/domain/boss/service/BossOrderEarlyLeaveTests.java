package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.*;
import com.kuaima.app.domain.boss.repository.*;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BossOrderEarlyLeaveTests {
    @Test
    void earlyLeaveMovesOrderToPendingSettlementAndCreatesSettlement() {
        BossOrderRespository orders = mock(BossOrderRespository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        BossOrderService service = new BossOrderService(orders, items, mock(UserRepository.class),
                mock(MessageService.class), settlements);
        BossOrder order = new BossOrder(); order.setId(45L); order.setSalary(200);
        order.setOrderStatus(BossStatus.ORDER_RECRUIT_END);
        BaseOrderItem item = new BaseOrderItem(); item.setId(70L); item.setOrderId(45L); item.setUserId(30L);
        item.setStatus(BossStatus.ITEM_ON_WORK); item.setWorkDate(Date.valueOf(LocalDate.now()));
        when(items.findById(70L)).thenReturn(Optional.of(item));
        when(orders.findById(45L)).thenReturn(Optional.of(order));
        when(orders.save(order)).thenReturn(order);
        when(items.findByOrderId(45L)).thenReturn(List.of(item));
        when(settlements.existsByItemIdAndStatusIn(eq(70L), any())).thenReturn(false);

        BossOrder result = service.finishByEarlyLeave(70L);

        assertEquals(BossStatus.ITEM_PENDING_SETTLE, item.getStatus());
        assertTrue(item.getEarlyLeave());
        assertEquals(BossStatus.ORDER_PENDING_SETTLE, result.getOrderStatus());
        verify(settlements).save(argThat(s -> s.getItemId().equals(70L)
                && s.getOrderId().equals(45L) && s.getWorkerId().equals(30L)
                && "待支付".equals(s.getStatus())));
    }
}

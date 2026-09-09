package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.constant.MessageType;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

class BossOrderManualReviewNotificationTests {

    @Test
    void manualReview_shouldNotifyBossEvenWhenSignNotifyIsDisabled() {
        BossOrderRespository orderRepository = mock(BossOrderRespository.class);
        BaseOrderItemRespository itemRepository = mock(BaseOrderItemRespository.class);
        UserRepository userRepository = mock(UserRepository.class);
        MessageService messageService = mock(MessageService.class);
        BossOrderService service = new BossOrderService(orderRepository, itemRepository,
                userRepository, messageService, mock(SettlementRespository.class));
        BossOrder order = new BossOrder();
        order.setId(21L);
        order.setCreateBy(1L);
        order.setOrderStatus(BossStatus.ORDER_RECRUITING);
        order.setOrderNum(1);
        order.setOrderTitle("五金厂CNC操作工");
        order.setPostion("CNC操作工");
        order.setSignMode("manual");
        order.setSignNotify(false);
        User worker = new User();
        worker.setNickname("测试零工");
        when(orderRepository.findById(21L)).thenReturn(Optional.of(order));
        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(worker));
        when(itemRepository.existsByOrderIdAndUserId(21L, 2L)).thenReturn(false);
        when(itemRepository.findByOrderId(21L)).thenReturn(List.of());
        when(itemRepository.save(any())).thenAnswer(invocation -> {
            BaseOrderItem item = invocation.getArgument(0);
            item.setId(31L);
            return item;
        });

        BaseOrderItem result = service.applyOrder(21L, 2L, null, false);

        assertEquals(BossStatus.ITEM_APPLIED, result.getStatus());
        verify(messageService).sendToUser(eq(1L), eq(UserRole.BOSS), eq(MessageType.ORDER_APPLY),
                eq("有新的报名待审核"), any(String.class), eq(BizType.ITEM), eq(31L));
    }
}

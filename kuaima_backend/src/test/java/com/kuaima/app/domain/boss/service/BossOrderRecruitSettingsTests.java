package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.constant.BossType;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

class BossOrderRecruitSettingsTests {

    private BossOrderRespository orderRepository;
    private BossOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(BossOrderRespository.class);
        service = new BossOrderService(
                orderRepository,
                mock(BaseOrderItemRespository.class),
                mock(UserRepository.class),
                mock(MessageService.class),
                mock(SettlementRespository.class));
    }

    @Test
    void create_shouldPersistRecruitSettingsAndDefaults() {
        BossOrder order = validOrder();
        order.setSignMode("manual");
        order.setPhoneNotify(false);
        order.setSignNotify(false);
        order.setStartRemind(true);
        order.setSettleNotify(false);
        when(orderRepository.save(any(BossOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BossOrder saved = service.createOrder(order);

        assertEquals("manual", saved.getSignMode());
        assertEquals(false, saved.getPhoneNotify());
        assertEquals(false, saved.getSignNotify());
        assertEquals(true, saved.getStartRemind());
        assertEquals(false, saved.getSettleNotify());
        assertEquals(BossStatus.ORDER_PENDING_AUDIT, saved.getOrderStatus());
    }

    @Test
    void create_shouldUseDefaultRecruitSettingsWhenOmitted() {
        BossOrder order = validOrder();
        when(orderRepository.save(any(BossOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BossOrder saved = service.createOrder(order);

        assertEquals("auto", saved.getSignMode());
        assertEquals(true, saved.getPhoneNotify());
        assertEquals(true, saved.getSignNotify());
        assertEquals(true, saved.getStartRemind());
        assertEquals(true, saved.getSettleNotify());
    }

    @Test
    void create_shouldRejectInvalidSignMode() {
        BossOrder order = validOrder();
        order.setSignMode("invalid");

        assertThrows(IllegalArgumentException.class, () -> service.createOrder(order));
    }

    @Test
    void detail_shouldReturnDefaultsForLegacyNullValues() {
        BossOrder order = new BossOrder();
        order.setId(9L);
        when(orderRepository.findById(9L)).thenReturn(Optional.of(order));

        BossOrder result = service.getOrder(9L);

        assertEquals("auto", result.getSignMode());
        assertEquals(true, result.getPhoneNotify());
        assertEquals(true, result.getSignNotify());
        assertEquals(true, result.getStartRemind());
        assertEquals(true, result.getSettleNotify());
    }

    @Test
    void update_shouldPersistExplicitFalseValuesWithoutResettingThem() {
        BossOrder existing = validOrder();
        existing.setId(12L);
        existing.setOrderStatus(BossStatus.ORDER_RECRUITING);
        existing.setSignMode("auto");
        existing.setPhoneNotify(true);
        existing.setSignNotify(true);
        existing.setStartRemind(true);
        existing.setSettleNotify(true);
        BossOrder update = new BossOrder();
        update.setSignMode("manual");
        update.setPhoneNotify(false);
        update.setSignNotify(false);
        update.setStartRemind(false);
        update.setSettleNotify(false);
        when(orderRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(BossOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BossOrder result = service.updateOrder(12L, update);

        assertEquals("manual", result.getSignMode());
        assertEquals(false, result.getPhoneNotify());
        assertEquals(false, result.getSignNotify());
        assertEquals(false, result.getStartRemind());
        assertEquals(false, result.getSettleNotify());
    }

    private BossOrder validOrder() {
        BossOrder order = new BossOrder();
        order.setOrderTitle("测试岗位");
        order.setType(BossType.DAILY);
        order.setPostion("普工");
        order.setOrderNum(1);
        order.setDuration(1);
        order.setSalary(100);
        order.setStartTime(new Date());
        return order;
    }
}

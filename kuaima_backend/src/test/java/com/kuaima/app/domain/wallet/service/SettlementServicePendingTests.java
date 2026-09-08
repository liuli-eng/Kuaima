package com.kuaima.app.domain.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.model.PendingSettlementModels.PendingSettlementOrder;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

class SettlementServicePendingTests {

    private BossOrderRespository orderRepository;
    private BaseOrderItemRespository itemRepository;
    private SettlementRespository settlementRepository;
    private UserRepository userRepository;
    private SettlementService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(BossOrderRespository.class);
        itemRepository = mock(BaseOrderItemRespository.class);
        settlementRepository = mock(SettlementRespository.class);
        userRepository = mock(UserRepository.class);
        service = new SettlementService(
                settlementRepository, orderRepository, itemRepository,
                mock(WalletService.class), mock(MessageService.class), userRepository);
    }

    @Test
    void listPendingByBoss_shouldAggregateUncreatedAndPendingItemsAndExcludePaidItems() {
        BossOrder order = order(10L, 1L, 200);
        BaseOrderItem uncreated = item(100L, 10L, 21L, BossStatus.ITEM_FINISHED);
        uncreated.setWorkDate(Date.valueOf("2026-09-01"));
        uncreated.setFinishDate(Date.valueOf("2026-09-02"));
        BaseOrderItem pendingItem = item(101L, 10L, 22L, BossStatus.ITEM_FINISHED);
        BaseOrderItem paidItem = item(102L, 10L, 23L, BossStatus.ITEM_FINISHED);
        Settlement pending = settlement(201L, 101L, 10L, 22L, 40000L, SettlementStatus.PENDING, 2);
        Settlement paid = settlement(202L, 102L, 10L, 23L, 20000L, SettlementStatus.PAID, 1);
        User worker1 = user(21L, "张师傅");
        User worker2 = user(22L, "李师傅");
        User worker3 = user(23L, "王师傅");

        when(orderRepository.findByCreateByOrderByIdDesc(1L)).thenReturn(List.of(order));
        when(itemRepository.findByBossIdJoinOrder(1L)).thenReturn(List.of(uncreated, pendingItem, paidItem));
        when(settlementRepository.findByItemIdInOrderByIdDesc(any())).thenReturn(List.of(pending, paid));
        when(userRepository.findAllById(any())).thenReturn(List.of(worker1, worker2, worker3));

        List<PendingSettlementOrder> result = service.listPendingByBoss(1L);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).workerCount());
        assertEquals(2, result.get(0).items().size());
        assertEquals("partial", result.get(0).status());
        assertEquals(80000L, result.get(0).amountFen());
        assertEquals("张师傅", result.get(0).items().get(0).workerName());
        assertEquals(201L, result.get(0).items().get(1).settlementId());
    }

    @Test
    void listPendingByBoss_shouldReturnEmptyForBossWithoutOrders() {
        when(orderRepository.findByCreateByOrderByIdDesc(9L)).thenReturn(List.of());

        assertEquals(List.of(), service.listPendingByBoss(9L));
    }

    @Test
    void pay_shouldCompleteOrderAfterAllFinishedItemsArePaid() {
        BossOrder order = order(11L, 1L, 200);
        order.setOrderStatus(BossStatus.ORDER_PENDING_SETTLE);
        BaseOrderItem finished = item(103L, 11L, 24L, BossStatus.ITEM_FINISHED);
        Settlement pending = settlement(203L, 103L, 11L, 24L, 20000L,
                SettlementStatus.PENDING, 1);
        pending.setWage(20000L);
        when(settlementRepository.findById(203L)).thenReturn(Optional.of(pending));
        when(settlementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));
        when(itemRepository.findByOrderId(11L)).thenReturn(List.of(finished));
        when(settlementRepository.findByOrderIdOrderByIdDesc(11L)).thenReturn(List.of(pending));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.mockPay(203L);

        assertEquals(BossStatus.ORDER_COMPLETED, order.getOrderStatus());
        verify(orderRepository).save(order);
    }

    private BossOrder order(Long id, Long bossId, int salary) {
        BossOrder order = new BossOrder();
        order.setId(id);
        order.setCreateBy(bossId);
        order.setOrderTitle("搬运工");
        order.setSalary(salary);
        return order;
    }

    private BaseOrderItem item(Long id, Long orderId, Long userId, String status) {
        BaseOrderItem item = new BaseOrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setUserId(userId);
        item.setStatus(status);
        return item;
    }

    private Settlement settlement(Long id, Long itemId, Long orderId, Long workerId,
                                  Long amount, String status, int workDays) {
        Settlement settlement = new Settlement();
        settlement.setId(id);
        settlement.setItemId(itemId);
        settlement.setOrderId(orderId);
        settlement.setWorkerId(workerId);
        settlement.setTotalAmount(amount);
        settlement.setStatus(status);
        settlement.setWorkDays(workDays);
        return settlement;
    }

    private User user(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        return user;
    }
}

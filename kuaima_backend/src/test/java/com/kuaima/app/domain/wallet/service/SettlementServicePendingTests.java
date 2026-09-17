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

    @Test
    void pay_shouldAdvanceAutoCreatedPendingItemAndCompleteOrder() {
        BossOrder order = order(14L, 1L, 200);
        order.setOrderStatus(BossStatus.ORDER_PENDING_SETTLE);
        BaseOrderItem pendingItem = item(105L, 14L, 24L, BossStatus.ITEM_PENDING_SETTLE);
        Settlement pending = settlement(204L, 105L, 14L, 24L, 20000L,
                SettlementStatus.PENDING, 1);
        pending.setWage(20000L);
        when(settlementRepository.findById(204L)).thenReturn(Optional.of(pending));
        when(settlementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.findById(105L)).thenReturn(Optional.of(pendingItem));
        when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(14L)).thenReturn(Optional.of(order));
        when(itemRepository.findByOrderId(14L)).thenReturn(List.of(pendingItem));
        when(settlementRepository.findByOrderIdOrderByIdDesc(14L)).thenReturn(List.of(pending));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.mockPay(204L);

        assertEquals(BossStatus.ITEM_FINISHED, pendingItem.getStatus());
        assertEquals(BossStatus.ORDER_COMPLETED, order.getOrderStatus());
        verify(itemRepository).save(pendingItem);
        verify(orderRepository).save(order);
    }

    @Test
    void paidRetry_shouldRepairOnWorkItemWithoutCreditingWalletAgain() {
        WalletService walletService = mock(WalletService.class);
        service = new SettlementService(
                settlementRepository, orderRepository, itemRepository,
                walletService, mock(MessageService.class), userRepository);
        BossOrder order = order(47L, 1L, 160);
        order.setOrderStatus(BossStatus.ORDER_PENDING_SETTLE);
        BaseOrderItem onWork = item(37L, 47L, 30L, BossStatus.ITEM_ON_WORK);
        Settlement paid = settlement(22L, 37L, 47L, 30L, 16000L,
                SettlementStatus.PAID, 1);
        paid.setWage(16000L);
        when(settlementRepository.findById(22L)).thenReturn(Optional.of(paid));
        when(itemRepository.findById(37L)).thenReturn(Optional.of(onWork));
        when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(47L)).thenReturn(Optional.of(order));
        when(itemRepository.findByOrderId(47L)).thenReturn(List.of(onWork));
        when(settlementRepository.findByOrderIdOrderByIdDesc(47L)).thenReturn(List.of(paid));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.mockPay(22L);

        assertEquals(BossStatus.ITEM_FINISHED, onWork.getStatus());
        assertEquals(BossStatus.ORDER_COMPLETED, order.getOrderStatus());
        verify(walletService, org.mockito.Mockito.never()).credit(any(), any(), any(), any(), any());
    }

    @Test
    void createSettlement_shouldAdvanceItemFromPendingSettleToFinished() {
        BaseOrderItem pendingSettle = item(110L, 12L, 25L, BossStatus.ITEM_PENDING_SETTLE);
        pendingSettle.setWorkDate(Date.valueOf("2026-09-10"));
        pendingSettle.setFinishDate(Date.valueOf("2026-09-10"));
        BossOrder order = order(12L, 2L, 300);
        when(itemRepository.findById(110L)).thenReturn(Optional.of(pendingSettle));
        when(settlementRepository.existsByItemIdAndStatusIn(eq(110L), any())).thenReturn(false);
        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));
        when(settlementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Settlement s = service.createSettlement(110L, null);

        assertEquals(SettlementStatus.PENDING, s.getStatus());
        assertEquals(30000L, s.getWage());
        assertEquals(30000L, s.getTotalAmount());
        assertEquals(BossStatus.ITEM_FINISHED, pendingSettle.getStatus());
        verify(itemRepository).save(pendingSettle);
    }

    @Test
    void listPendingByBoss_shouldIncludePendingSettleItems() {
        BossOrder order = order(13L, 1L, 200);
        BaseOrderItem pendingSettle = item(104L, 13L, 24L, BossStatus.ITEM_PENDING_SETTLE);
        pendingSettle.setWorkDate(Date.valueOf("2026-09-10"));
        pendingSettle.setFinishDate(Date.valueOf("2026-09-10"));
        User worker = user(24L, "赵师傅");
        when(orderRepository.findByCreateByOrderByIdDesc(1L)).thenReturn(List.of(order));
        when(itemRepository.findByBossIdJoinOrder(1L)).thenReturn(List.of(pendingSettle));
        when(userRepository.findAllById(any())).thenReturn(List.of(worker));
        when(settlementRepository.findByItemIdInOrderByIdDesc(any())).thenReturn(List.of());

        List<PendingSettlementOrder> result = service.listPendingByBoss(1L);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).items().size());
        assertEquals(20000L, result.get(0).items().get(0).amountFen());
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

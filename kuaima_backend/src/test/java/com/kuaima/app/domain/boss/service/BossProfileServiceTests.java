package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.constant.SettlementStatus;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

class BossProfileServiceTests {
    @Test
    void stats_shouldReturnCompleteZeroStructure() {
        BossOrderRespository orders = mock(BossOrderRespository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        UserRepository users = mock(UserRepository.class);
        when(orders.findByCreateByOrderByIdDesc(1L)).thenReturn(List.of());
        when(users.findById(1L)).thenReturn(Optional.empty());

        var result = new BossProfileService(orders, items, settlements, users).stats(1L);

        assertEquals(0, result.totalOrders());
        assertEquals(0, result.integrityScore());
        assertEquals(0, result.goodRate());
        assertEquals(0, result.arrivalRate());
        assertEquals(0, result.settleRate());
        assertEquals(0, result.totalPayment());
    }

    @Test
    void stats_shouldAggregateArrivalSettlementAndPaymentInBatch() {
        BossOrderRespository orders = mock(BossOrderRespository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        UserRepository users = mock(UserRepository.class);
        BossOrder completed = order(10L, BossStatus.ORDER_COMPLETED);
        BossOrder recruiting = order(11L, BossStatus.ORDER_RECRUITING);
        BaseOrderItem hired = item(20L, BossStatus.ITEM_HIRED, null);
        BaseOrderItem finished = item(21L, BossStatus.ITEM_FINISHED, LocalDate.of(2026, 9, 14));
        BaseOrderItem canceled = item(22L, BossStatus.ITEM_CANCELED, null);
        Settlement paidFast = settlement(21L, 10_000L, LocalDateTime.of(2026, 9, 14, 20, 0));
        Settlement paidSlow = settlement(20L, 20_000L, LocalDateTime.of(2026, 9, 16, 1, 0));
        User boss = new User(); boss.setCreditScore(709);
        when(orders.findByCreateByOrderByIdDesc(1L)).thenReturn(List.of(completed, recruiting));
        when(items.findAllByBossId(1L)).thenReturn(List.of(hired, finished, canceled));
        when(settlements.findByOrderIdIn(List.of(10L, 11L))).thenReturn(List.of(paidFast, paidSlow));
        when(users.findById(1L)).thenReturn(Optional.of(boss));

        var result = new BossProfileService(orders, items, settlements, users).stats(1L);

        assertEquals(2, result.totalOrders());
        assertEquals(1, result.recruitingCount());
        assertEquals(2, result.applicantCount());
        assertEquals(709, result.integrityScore());
        assertEquals(50, result.arrivalRate());
        assertEquals(100, result.settleRate());
        assertEquals(30_000L, result.totalPayment());
        assertEquals(300.0, result.settledAmount());
        assertEquals(1, result.completedOrders());
    }

    private BossOrder order(Long id, String status) { BossOrder o = new BossOrder(); o.setId(id); o.setOrderStatus(status); return o; }
    private BaseOrderItem item(Long id, String status, LocalDate finish) { BaseOrderItem i = new BaseOrderItem(); i.setId(id); i.setStatus(status); if (finish != null) i.setFinishDate(Date.valueOf(finish)); return i; }
    private Settlement settlement(Long itemId, long amount, LocalDateTime paidAt) { Settlement s = new Settlement(); s.setItemId(itemId); s.setStatus(SettlementStatus.PAID); s.setTotalAmount(amount); s.setPayTime(paidAt); return s; }
}

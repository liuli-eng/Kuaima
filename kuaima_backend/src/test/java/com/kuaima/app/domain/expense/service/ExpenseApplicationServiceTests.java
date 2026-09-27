package com.kuaima.app.domain.expense.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.expense.dto.ExpenseApplicationRequest;
import com.kuaima.app.domain.expense.entity.ExpenseApplication;
import com.kuaima.app.domain.expense.repository.ExpenseApplicationRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

class ExpenseApplicationServiceTests {
    private ExpenseApplicationRepository applications;
    private BossOrderRespository orders;
    private UserRepository users;
    private ExpenseApplicationService service;

    @BeforeEach
    void setUp() {
        applications = mock(ExpenseApplicationRepository.class);
        orders = mock(BossOrderRespository.class);
        users = mock(UserRepository.class);
        User boss = new User(); boss.setId(7L); boss.setEnterpriseStatus("APPROVED");
        when(users.findById(7L)).thenReturn(Optional.of(boss));
        service = new ExpenseApplicationService(applications, orders, users);
    }

    @Test
    void createsPendingApplicationAndMarksLargeAmountForManualReview() {
        BossOrder order = order(123L, 7L, BossStatus.ORDER_PENDING_SETTLE);
        when(applications.findByBossIdAndIdempotencyKey(7L, "request-1")).thenReturn(Optional.empty());
        when(orders.findById(123L)).thenReturn(Optional.of(order));
        when(applications.save(any())).thenAnswer(invocation -> {
            ExpenseApplication saved = invocation.getArgument(0);
            saved.setId(1001L);
            return saved;
        });

        ExpenseApplicationRequest request = request(new BigDecimal("500.01"));
        ExpenseApplication result = service.create(request, new LoginUser(7L, "boss", "BOSS"), "request-1");

        assertEquals("PENDING", result.getStatus());
        assertEquals(true, result.isManualReview());
        assertEquals(1001L, result.getId());
        verify(applications).save(any(ExpenseApplication.class));
    }

    @Test
    void rejectsOrderOwnedByAnotherUser() {
        when(applications.findByBossIdAndIdempotencyKey(7L, "request-2")).thenReturn(Optional.empty());
        when(orders.findById(123L)).thenReturn(Optional.of(order(123L, 8L, BossStatus.ORDER_COMPLETED)));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(request(new BigDecimal("88.50")),
                        new LoginUser(7L, "boss", "BOSS"), "request-2"));
        verify(applications, never()).save(any());
    }

    @Test
    void rejectsNonBossAndDisallowedOrderStatus() {
        assertThrows(ForbiddenBusinessException.class,
                () -> service.create(request(new BigDecimal("1.00")),
                        new LoginUser(7L, "worker", "USER"), "request-3"));

        when(applications.findByBossIdAndIdempotencyKey(7L, "request-4")).thenReturn(Optional.empty());
        when(orders.findById(123L)).thenReturn(Optional.of(order(123L, 7L, BossStatus.ORDER_RECRUITING)));
        assertThrows(IllegalArgumentException.class,
                () -> service.create(request(new BigDecimal("1.00")),
                        new LoginUser(7L, "boss", "BOSS"), "request-4"));
    }

    @Test
    void idempotencyReturnsExistingApplicationWithoutCreatingAnother() {
        ExpenseApplication existing = new ExpenseApplication();
        existing.setId(9L); existing.setStatus("PENDING");
        when(applications.findByBossIdAndIdempotencyKey(7L, "same-key")).thenReturn(Optional.of(existing));

        ExpenseApplication result = service.create(request(new BigDecimal("88.50")),
                new LoginUser(7L, "boss", "BOSS"), "same-key");

        assertEquals(9L, result.getId());
        verifyNoInteractions(orders);
        verify(applications, never()).save(any());
    }

    private ExpenseApplicationRequest request(BigDecimal amount) {
        ExpenseApplicationRequest request = new ExpenseApplicationRequest();
        request.setType("TRANSPORT"); request.setOrderId(123L); request.setAmount(amount);
        request.setReason("订单加班产生的夜宵费用"); request.setAttachments(List.of("/uploads/receipt.jpg"));
        return request;
    }

    private BossOrder order(Long id, Long owner, String status) {
        BossOrder order = new BossOrder(); order.setId(id); order.setCreateBy(owner);
        order.setOrderTitle("测试订单"); order.setOrderStatus(status); return order;
    }
}

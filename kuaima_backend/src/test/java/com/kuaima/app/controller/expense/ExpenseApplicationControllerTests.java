package com.kuaima.app.controller.expense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.kuaima.app.domain.expense.dto.ExpenseApplicationRequest;
import com.kuaima.app.domain.expense.entity.ExpenseApplication;
import com.kuaima.app.domain.expense.service.ExpenseApplicationService;
import com.kuaima.app.security.model.LoginUser;

class ExpenseApplicationControllerTests {
    @Test
    void createReturnsRequiredSummaryFields() {
        ExpenseApplicationService service = mock(ExpenseApplicationService.class);
        ExpenseApplicationController controller = new ExpenseApplicationController(service);
        ExpenseApplication saved = new ExpenseApplication();
        saved.setId(1001L); saved.setStatus("PENDING"); saved.setManualReview(false);
        saved.setCreateTime(LocalDateTime.of(2026, 9, 27, 10, 30));
        when(service.create(any(), any(), any())).thenReturn(saved);
        ExpenseApplicationRequest request = new ExpenseApplicationRequest();
        request.setType("TRANSPORT"); request.setOrderId(123L); request.setAmount(new BigDecimal("88.50"));
        request.setReason("订单加班产生的夜宵费用"); request.setAttachments(List.of());

        var result = controller.create(request, "key-1", auth(7L, "BOSS"));

        assertEquals(200, result.getCode());
        assertEquals(1001L, result.getData().get("id"));
        assertEquals("PENDING", result.getData().get("status"));
        assertEquals("2026-09-27 10:30:00", result.getData().get("createTime"));
    }

    @Test
    void listRejectsUnauthenticatedRequest() {
        ExpenseApplicationController controller = new ExpenseApplicationController(mock(ExpenseApplicationService.class));
        org.junit.jupiter.api.Assertions.assertThrows(com.kuaima.app.common.ForbiddenBusinessException.class,
                () -> controller.list(null, 0, 20, null));
    }

    private UsernamePasswordAuthenticationToken auth(Long id, String role) {
        return new UsernamePasswordAuthenticationToken(new LoginUser(id, "boss", role), null);
    }
}

package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.CreditFlowRepository;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.user.service.CreditScoreService;
import com.kuaima.app.security.model.LoginUser;

class AdminCreditControllerTests {
    @Test
    void workerListUsesWorkerStarScoreType() {
        UserRepository users = mock(UserRepository.class);
        CreditFlowRepository flows = mock(CreditFlowRepository.class);
        CreditScoreService scores = mock(CreditScoreService.class);
        User worker = user(7L, false);
        when(users.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(worker));
        when(flows.findByUserIdAndScoreTypeOrderByTimestampDesc(7L, CreditScoreService.WORKER_STAR)).thenReturn(List.of());

        var result = new AdminCreditController(users, scores, flows).users(null, null, null, 0, 10, admin());

        assertEquals(CreditScoreService.WORKER_STAR, result.getData().getContent().get(0).get("scoreType"));
        verify(flows).findByUserIdAndScoreTypeOrderByTimestampDesc(7L, CreditScoreService.WORKER_STAR);
    }

    @Test
    void adjustRequiresTheIdentitySpecificScoreTypeAndWritesAdjustment() {
        UserRepository users = mock(UserRepository.class);
        CreditFlowRepository flows = mock(CreditFlowRepository.class);
        CreditScoreService scores = mock(CreditScoreService.class);
        User worker = user(7L, false);
        when(users.findById(7L)).thenReturn(Optional.of(worker));
        when(scores.adjust(eq(7L), eq(CreditScoreService.WORKER_STAR), eq(-3), anyString(), eq("ADMIN"), anyString(), eq("原因")))
                .thenReturn(true);

        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("scoreType", CreditScoreService.WORKER_STAR); body.put("delta", -3); body.put("reason", "原因");
        var controller = new AdminCreditController(users, scores, flows);
        controller.adjust(7L, body, admin());

        verify(scores).adjust(eq(7L), eq(CreditScoreService.WORKER_STAR), eq(-3), eq("ADMIN_MANUAL_ADJUST"),
                eq("ADMIN"), anyString(), eq("原因"));
    }

    private User user(Long id, boolean boss) {
        User user = new User(); user.setId(id); user.setCreditScore(80); user.setStarScore(70);
        if (boss) user.setEnterpriseStatus("APPROVED");
        return user;
    }

    private UsernamePasswordAuthenticationToken admin() {
        return new UsernamePasswordAuthenticationToken(new LoginUser(99L, "admin", "ADMIN_ADMIN"), null);
    }
}

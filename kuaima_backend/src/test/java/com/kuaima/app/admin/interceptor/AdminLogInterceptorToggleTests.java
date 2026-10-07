package com.kuaima.app.admin.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.kuaima.app.admin.repository.MessageTemplateRepository;
import com.kuaima.app.admin.service.AdminLogService;
import com.kuaima.app.domain.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class AdminLogInterceptorToggleTests {

    @Test
    void academyToggleDoesNotQueryMessageTemplateAndLogsAsynchronously() {
        AdminLogService logs = mock(AdminLogService.class);
        MessageTemplateRepository templates = mock(MessageTemplateRepository.class);
        var interceptor = new AdminLogInterceptor(logs, templates, mock(UserRepository.class));
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/admin/academy/simulate-videos/2/toggle");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(templates, never()).findById(any());
        verify(logs).recordAsync(eq("system"), eq(null), eq("启用/禁用"),
                eq("后台操作 #2"), eq("127.0.0.1"), eq("成功"), any());
        verify(logs, never()).record(any(), any(), any(), any(), any(), any(), any());
    }
}

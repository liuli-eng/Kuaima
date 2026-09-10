package com.kuaima.app.admin.interceptor;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 对 /admin/** 的 JSON 请求包装为 ContentCachingRequestWrapper，
 * 使 AdminLogInterceptor 能在 afterCompletion 阶段读取已被 Controller 消费过的请求体，
 * 用于区分「启用/禁用」等写操作（请求体中携带 status 字段）。
 */
@Component
public class AdminBodyCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String contentType = request.getContentType();
        boolean json = contentType != null && contentType.toLowerCase().contains("application/json");
        if (uri != null && uri.startsWith("/admin/") && json) {
            // Spring 7 要求显式传入缓存上限（1MB，足够容纳请求体 JSON）
            filterChain.doFilter(new ContentCachingRequestWrapper(request, 1024 * 1024), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

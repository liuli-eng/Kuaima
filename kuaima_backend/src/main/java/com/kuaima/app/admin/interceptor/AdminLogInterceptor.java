package com.kuaima.app.admin.interceptor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.kuaima.app.admin.service.AdminLogService;
import com.kuaima.app.security.model.LoginUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 操作日志拦截器：自动记录 /admin/** 下的所有写操作（POST/PUT/DELETE）。
 * 登录接口（/admin/auth/login）由 AdminAuthController 自行记录，此处跳过。
 * 日志查询接口（/admin/logs）跳过，避免递归。
 */
@Component
public class AdminLogInterceptor implements HandlerInterceptor {

    private final AdminLogService logService;

    public AdminLogInterceptor(AdminLogService logService) {
        this.logService = logService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 只记录 admin 后台接口
        if (uri == null || !uri.startsWith("/admin/")) return;
        // 跳过登录（登录由控制器记录，此时 SecurityContext 为空）
        if (uri.startsWith("/admin/auth/login")) return;
        // 跳过日志查询自身，避免递归
        if (uri.startsWith("/admin/logs")) return;
        // 只读 GET 不记录
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) return;

        // 读取当前登录管理员
        String operator = "system";
        Long operatorId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser u) {
            operator = u.username();
            operatorId = u.id();
        }

        String type = resolveType(method, uri);
        String target = uri;
        String result = (response.getStatus() >= 200 && response.getStatus() < 300) ? "成功" : "失败";
        String ip = request.getRemoteAddr();
        String detail = method + " " + uri + (ex != null ? " | ex=" + ex.getMessage() : "");

        logService.record(operator, operatorId, type, target, ip, result, detail);
    }

    /** 根据 HTTP 方法和路径推断操作类型 */
    private String resolveType(String method, String uri) {
        String lower = uri.toLowerCase();
        if ("POST".equalsIgnoreCase(method)) {
            return "新建";
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            return "删除";
        }
        // PUT
        if (lower.contains("audit") || lower.contains("certification") || lower.contains("approve") || lower.contains("reject")) {
            return "审核";
        }
        if (lower.contains("reset-password")) {
            return "重置密码";
        }
        if (lower.contains("/admin-users") && (lower.contains("status") || lower.contains("enable") || lower.contains("disable"))) {
            return "启用/禁用";
        }
        return "编辑";
    }
}

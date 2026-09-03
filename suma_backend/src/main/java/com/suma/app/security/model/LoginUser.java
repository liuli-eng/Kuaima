package com.suma.app.security.model;

/**
 * 已登录用户（JWT 过滤器解析 token 后写入 SecurityContext 的 principal）。
 * 携带数据库用户ID，供审计(getCurrentAuditor)、业务取当前用户直接使用，无需再按 username 反查。
 */
public record LoginUser(Long id, String username, String role) {
}

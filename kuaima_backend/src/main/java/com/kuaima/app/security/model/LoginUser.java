package com.kuaima.app.security.model;

/**
 * 已登录用户（JWT 过滤器解析 token 后写入 SecurityContext 的 principal）。
 * 携带数据库用户ID，供审计(getCurrentAuditor)、业务取当前用户直接使用，无需再按 username 反查。
 */
public record LoginUser(Long id, String username, String role, Long accountGroupOwnerId,
                        Long enterpriseId, String memberRole) {

    /** 普通登录没有独立账号组声明，账号组默认归属当前用户。 */
    public LoginUser(Long id, String username, String role) {
        this(id, username, role, id, null, null);
    }

    public LoginUser(Long id, String username, String role, Long accountGroupOwnerId) {
        this(id, username, role, accountGroupOwnerId, null, null);
    }

    public Long accountGroupOwnerId() {
        return accountGroupOwnerId == null ? id : accountGroupOwnerId;
    }
}

package com.kuaima.app.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.kuaima.app.security.model.LoginUser;

@Component
public class SecurityAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        // 只从认证主体读取 uid，禁止在 JPA 审计回调中查询 UserRepository。
        // 审计回调发生在 flush/update 期间，再查询数据库会触发递归 flush，最终导致 StackOverflowError。
        if (principal instanceof LoginUser loginUser) {
            return Optional.ofNullable(loginUser.id());
        }
        return Optional.empty();
    }
}

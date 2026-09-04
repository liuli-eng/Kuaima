package com.kuaima.app.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

@Component
public class SecurityAuditorAware implements AuditorAware<Long> {

    private final UserRepository userRepository;

    public SecurityAuditorAware(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        // 优先取 JWT 载荷中的用户ID(uid)，无需按 username 反查；
        // uid 缺失(旧 token)或非 LoginUser 认证时，回退按 username 反查。
        if (principal instanceof LoginUser loginUser) {
            if (loginUser.id() != null) {
                return Optional.of(loginUser.id());
            }
            return userRepository.findByUsername(loginUser.username()).map(User::getId);
        }
        if (principal instanceof String username) {
            return userRepository.findByUsername(username).map(User::getId);
        }
        return Optional.empty();
    }
}

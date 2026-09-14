package com.kuaima.app.controller.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.subaccount.entity.SubAccount;
import com.kuaima.app.domain.subaccount.repository.SubAccountRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

class SubAccountControllerTests {
    @Test
    void list_shouldRejectAnotherBossParentId() {
        SubAccountController controller = controller(mock(SubAccountRepository.class), mock(UserRepository.class), true);
        assertThrows(ForbiddenBusinessException.class,
                () -> controller.listSubAccounts(9L, bossAuthentication(1L)));
    }

    @Test
    void delete_shouldRejectAnotherBossResource() {
        SubAccountRepository repository = mock(SubAccountRepository.class);
        when(repository.findByIdAndParentId(10L, 1L)).thenReturn(Optional.empty());
        when(repository.existsById(10L)).thenReturn(true);
        SubAccountController controller = controller(repository, mock(UserRepository.class), true);
        assertThrows(ForbiddenBusinessException.class,
                () -> controller.deleteSubAccount(10L, bossAuthentication(1L)));
    }

    @Test
    void create_shouldUseJwtBossAndConfiguredDevCode() {
        SubAccountRepository repository = mock(SubAccountRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findByPhone("13800000000")).thenReturn(List.of());
        when(users.findByUsername(any())).thenReturn(Optional.empty());
        when(users.save(any(User.class))).thenAnswer(invocation -> { User u = invocation.getArgument(0); u.setId(1002L); return u; });
        when(repository.existsByParentIdAndUserId(1L, 1002L)).thenReturn(false);
        when(repository.save(any(SubAccount.class))).thenAnswer(invocation -> { SubAccount s = invocation.getArgument(0); s.setId(10L); return s; });

        var result = controller(repository, users, true).createSubAccount(
                Map.of("phone", "13800000000", "code", "123456", "role", "OPERATOR"), bossAuthentication(1L));

        assertEquals(10L, result.getData().getId());
        assertEquals(1L, result.getData().getParentId());
        assertEquals(1002L, result.getData().getUserId());
        assertEquals("13800000000", result.getData().getPhone());
        assertEquals("ACTIVE", result.getData().getStatus());
    }

    @Test
    void create_shouldRejectCodeWhenNotInDevProfile() {
        SubAccountController controller = controller(mock(SubAccountRepository.class), mock(UserRepository.class), false);
        assertThrows(IllegalStateException.class, () -> controller.createSubAccount(
                Map.of("phone", "13800000000", "code", "123456", "role", "OPERATOR"), bossAuthentication(1L)));
    }

    private SubAccountController controller(SubAccountRepository repository, UserRepository users, boolean dev) {
        PasswordEncoder encoder = mock(PasswordEncoder.class); when(encoder.encode(any())).thenReturn("encoded");
        Environment environment = mock(Environment.class); when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(dev);
        when(environment.getProperty("aliyun.sms.mock-enabled", "false")).thenReturn(Boolean.toString(dev));
        return new SubAccountController(repository, users, encoder, environment, "123456");
    }

    private Authentication bossAuthentication(Long id) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(id, "boss", "BOSS"));
        return authentication;
    }
}

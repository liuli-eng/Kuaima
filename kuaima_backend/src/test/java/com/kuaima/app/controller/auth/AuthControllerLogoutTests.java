package com.kuaima.app.controller.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.util.JwtUtil;
import com.kuaima.app.service.SmsService;
import com.kuaima.app.wechat.service.WechatService;

class AuthControllerLogoutTests {

    @Test
    void logout_shouldReturnSuccess() {
        AuthController controller = new AuthController(
                mock(UserRepository.class),
                mock(PasswordEncoder.class),
                mock(JwtUtil.class),
                mock(WechatService.class),
                mock(SmsService.class));

        var result = controller.logout();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }
}

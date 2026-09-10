package com.kuaima.app.security.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilWechatRegistrationTests {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-that-is-at-least-32-bytes-long", 1800000L);
    }

    @Test
    void registrationToken_shouldCarryOpenid() {
        String token = jwtUtil.generateWechatRegistrationToken("openid-1");

        assertEquals("openid-1", jwtUtil.getWechatRegistrationOpenid(token));
    }

    @Test
    void accessToken_shouldNotBeAcceptedAsRegistrationToken() {
        String accessToken = jwtUtil.generateAccessToken("user", "USER", 1L);

        assertThrows(IllegalArgumentException.class,
                () -> jwtUtil.getWechatRegistrationOpenid(accessToken));
    }
}

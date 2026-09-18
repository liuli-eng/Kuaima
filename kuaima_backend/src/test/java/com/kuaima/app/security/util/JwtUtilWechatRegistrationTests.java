package com.kuaima.app.security.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void accessToken_shouldCarryAccountGroupOwnerIdOnlyForQuickLogin() {
        String normalToken = jwtUtil.generateAccessToken("boss", "BOSS", 7L);
        String quickLoginToken = jwtUtil.generateAccessToken("boss-31", "BOSS", 31L, 7L);

        assertNull(jwtUtil.getAccountGroupOwnerId(normalToken));
        assertEquals(7L, jwtUtil.getAccountGroupOwnerId(quickLoginToken));
        assertEquals(31L, jwtUtil.getUserId(quickLoginToken));
    }
}

package com.kuaima.app.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kuaima.app.security.model.LoginUser;
import com.kuaima.app.security.util.JwtUtil;

class JwtAuthenticationFilterTests {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void filter_shouldCarryAccountGroupOwnerIdForQuickLoginToken() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer quick-login-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doNothing().when(chain).doFilter(request, response);
        when(jwtUtil.isAccessToken("quick-login-token")).thenReturn(true);
        when(jwtUtil.getUsername("quick-login-token")).thenReturn("boss-31");
        when(jwtUtil.getRole("quick-login-token")).thenReturn("BOSS");
        when(jwtUtil.getUserId("quick-login-token")).thenReturn(31L);
        when(jwtUtil.getAccountGroupOwnerId("quick-login-token")).thenReturn(7L);

        filter.doFilterInternal(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser principal = (LoginUser) authentication.getPrincipal();
        assertEquals(31L, principal.id());
        assertEquals(7L, principal.accountGroupOwnerId());
    }

    @Test
    void filter_shouldFallbackGroupOwnerToUserIdForNormalToken() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer normal-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doNothing().when(chain).doFilter(request, response);
        when(jwtUtil.isAccessToken("normal-token")).thenReturn(true);
        when(jwtUtil.getUsername("normal-token")).thenReturn("boss");
        when(jwtUtil.getRole("normal-token")).thenReturn("BOSS");
        when(jwtUtil.getUserId("normal-token")).thenReturn(7L);
        when(jwtUtil.getAccountGroupOwnerId("normal-token")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        LoginUser principal = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(7L, principal.accountGroupOwnerId());
    }
}

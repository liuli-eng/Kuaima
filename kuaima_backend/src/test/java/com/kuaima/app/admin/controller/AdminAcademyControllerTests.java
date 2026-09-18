package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.admin.entity.AdminUser;
import com.kuaima.app.admin.repository.AdminUserRepository;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.academy.service.AcademyAdminService;
import com.kuaima.app.security.model.LoginUser;

class AdminAcademyControllerTests {
    @Test
    void listShouldRequireAdminPrincipal() {
        var service = mock(AcademyAdminService.class);
        var admins = mock(AdminUserRepository.class);
        var controller = new AdminAcademyController(service, admins);
        Authentication authentication = authentication(new LoginUser(1L, "worker", "USER"));

        assertThrows(ForbiddenBusinessException.class, () -> controller.simulateVideos(authentication));
    }

    @Test
    void listShouldAllowEnabledAdmin() {
        var service = mock(AcademyAdminService.class);
        var admins = mock(AdminUserRepository.class);
        var controller = new AdminAcademyController(service, admins);
        Authentication authentication = authentication(new LoginUser(1L, "admin", "ADMIN_ADMIN"));
        AdminUser admin = new AdminUser();
        admin.setId(1L);
        admin.setStatus("启用");
        when(admins.findById(1L)).thenReturn(Optional.of(admin));
        when(service.simulateVideos()).thenReturn(List.of());

        var result = controller.simulateVideos(authentication);

        assertEquals(200, result.getCode());
        verify(service).simulateVideos();
    }

    @Test
    void writeShouldRejectViewerAndDisabledAdmin() {
        var service = mock(AcademyAdminService.class);
        var admins = mock(AdminUserRepository.class);
        var controller = new AdminAcademyController(service, admins);
        Authentication viewer = authentication(new LoginUser(1L, "viewer", "ADMIN_VIEWER"));

        assertThrows(ForbiddenBusinessException.class, () ->
                controller.deleteQuiz(1L, viewer));
    }

    private Authentication authentication(LoginUser user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        return authentication;
    }
}

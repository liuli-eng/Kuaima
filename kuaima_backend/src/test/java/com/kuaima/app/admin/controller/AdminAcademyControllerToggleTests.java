package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.kuaima.app.admin.repository.AdminUserRepository;
import com.kuaima.app.domain.academy.service.AcademyAdminService;
import com.kuaima.app.security.model.LoginUser;

class AdminAcademyControllerToggleTests {

    @Test
    void toggleDelegatesAdminValidationToAtomicUpdate() {
        AcademyAdminService service = mock(AcademyAdminService.class);
        AdminUserRepository admins = mock(AdminUserRepository.class);
        when(service.toggleSimulateVideo(2L, null, 9L))
                .thenReturn(Map.of("id", 2L, "enabled", true));
        var controller = new AdminAcademyController(service, admins);
        var authentication = new UsernamePasswordAuthenticationToken(
                new LoginUser(9L, "admin", "ADMIN_ADMIN"), null);

        var result = controller.toggleSimulateVideo(2L, null, authentication);

        assertEquals(true, result.getData().get("enabled"));
        verify(service).toggleSimulateVideo(2L, null, 9L);
        verify(admins, never()).findById(9L);
    }
}

package com.kuaima.app.controller.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.domain.boss.service.BossAttendanceCodeService;
import com.kuaima.app.domain.enterprise.service.EnterpriseContextService;
import com.kuaima.app.security.model.LoginUser;

class BossAttendanceCodeControllerTests {
    @Test
    void newPersonalBossWithoutEnterprise_shouldUsePersonalAttendanceCode() {
        BossAttendanceCodeService service = mock(BossAttendanceCodeService.class);
        EnterpriseContextService contexts = mock(EnterpriseContextService.class);
        BossAttendanceCodeController controller = new BossAttendanceCodeController(service, contexts);
        Authentication authentication = authentication(new LoginUser(7L, "boss", "BOSS"));
        when(service.today(7L)).thenReturn(Map.of("workCode", "1234"));

        var result = controller.today(authentication);

        assertEquals("1234", result.getData().get("workCode"));
        verify(service).today(7L);
    }

    private Authentication authentication(LoginUser principal) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        return authentication;
    }
}

package com.kuaima.app.controller.boss;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.service.BossHomeService;
import com.kuaima.app.security.model.LoginUser;

class BossHomeControllerTests {
    @Test
    void overview_shouldRejectNonBossPrincipal() {
        BossHomeController controller = new BossHomeController(mock(BossHomeService.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(2L, "worker", "USER"));
        assertThrows(ForbiddenBusinessException.class,
                () -> controller.overview(null, null, null, null, authentication));
    }

    @Test
    void schedule_shouldRejectMissingAuthentication() {
        BossHomeController controller = new BossHomeController(mock(BossHomeService.class));
        assertThrows(ForbiddenBusinessException.class,
                () -> controller.schedule(LocalDate.of(2026, 9, 14), null, null));
    }
}

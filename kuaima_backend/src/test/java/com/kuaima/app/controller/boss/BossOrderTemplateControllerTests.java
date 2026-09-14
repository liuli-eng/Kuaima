package com.kuaima.app.controller.boss;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.service.BossOrderTemplateService;
import com.kuaima.app.security.model.LoginUser;

class BossOrderTemplateControllerTests {
    @Test
    void detail_shouldRejectNonBossPrincipal() {
        BossOrderTemplateController controller = new BossOrderTemplateController(mock(BossOrderTemplateService.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(1L, "worker", "USER"));
        assertThrows(ForbiddenBusinessException.class, () -> controller.detail(10L, authentication));
    }

    @Test
    void rename_shouldRejectMissingAuthentication() {
        BossOrderTemplateController controller = new BossOrderTemplateController(mock(BossOrderTemplateService.class));
        assertThrows(ForbiddenBusinessException.class, () -> controller.rename(10L, null, null));
    }
}

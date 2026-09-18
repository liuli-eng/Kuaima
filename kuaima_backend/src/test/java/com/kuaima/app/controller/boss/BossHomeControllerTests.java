package com.kuaima.app.controller.boss;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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
    void overview_shouldPassCurrentUserIdAndAccountGroupOwnerId() {
        BossHomeService homeService = mock(BossHomeService.class);
        BossHomeController controller = new BossHomeController(homeService);
        Authentication authentication = authentication(31L, 7L);

        controller.overview(null, null, null, null, authentication);

        verify(homeService).overview(31L, 7L, null, null);
    }

    @Test
    void schedule_shouldRejectMissingAuthentication() {
        BossHomeController controller = new BossHomeController(mock(BossHomeService.class));
        assertThrows(ForbiddenBusinessException.class,
                () -> controller.schedule(LocalDate.of(2026, 9, 14), null, null));
    }

    @Test
    void schedule_shouldPassCurrentUserIdAndAccountGroupOwnerId() {
        BossHomeService homeService = mock(BossHomeService.class);
        BossHomeController controller = new BossHomeController(homeService);
        LocalDate date = LocalDate.of(2026, 9, 17);

        controller.schedule(date, null, authentication(31L, 7L));

        verify(homeService).schedule(31L, 7L, date, null);
    }

    private Authentication authentication(Long id, Long accountGroupOwnerId) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal())
                .thenReturn(new LoginUser(id, "boss", "BOSS", accountGroupOwnerId));
        return authentication;
    }
}

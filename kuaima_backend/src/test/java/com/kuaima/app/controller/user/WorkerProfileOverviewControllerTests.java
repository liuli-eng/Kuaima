package com.kuaima.app.controller.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.model.WorkerProfileOverview;
import com.kuaima.app.domain.user.service.WorkerProfileOverviewService;
import com.kuaima.app.security.model.LoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class WorkerProfileOverviewControllerTests {
    @Test
    void usesWorkerIdFromJwt() {
        WorkerProfileOverviewService service = mock(WorkerProfileOverviewService.class);
        WorkerProfileOverviewController controller = new WorkerProfileOverviewController(service);
        Authentication authentication = mock(Authentication.class);
        var overview = new WorkerProfileOverview(1, 30, 0, 0, 0, 0, 0, 0, 0, 0);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(30L, "worker", UserRole.USER));
        when(service.overview(30L)).thenReturn(overview);

        assertSame(overview, controller.overview(authentication).getData());
        verify(service).overview(30L);
    }

    @Test
    void rejectsBossJwt() {
        WorkerProfileOverviewController controller = new WorkerProfileOverviewController(mock(WorkerProfileOverviewService.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(30L, "boss", UserRole.BOSS));
        assertThrows(ForbiddenBusinessException.class, () -> controller.overview(authentication));
    }
}

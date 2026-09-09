package com.kuaima.app.controller.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.model.WorkerProfileModels.WorkerProfile;
import com.kuaima.app.domain.user.service.WorkerProfileService;
import com.kuaima.app.security.model.LoginUser;

class WorkerProfileControllerTests {

    @Test
    void getProfile_shouldUseCurrentWorkerId() {
        WorkerProfileService service = mock(WorkerProfileService.class);
        WorkerProfileController controller = new WorkerProfileController(service);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(30L, "worker", UserRole.USER));
        when(service.getProfile(30L)).thenReturn(new WorkerProfile(
                30L, null, "零工", "13800005678", null, null,
                null, null, null, null, null));

        var result = controller.getProfile(authentication);

        assertEquals(30L, result.getData().id());
        verify(service).getProfile(30L);
    }

    @Test
    void getProfile_shouldRejectBossIdentity() {
        WorkerProfileController controller = new WorkerProfileController(mock(WorkerProfileService.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(30L, "boss", UserRole.BOSS));

        assertThrows(ForbiddenBusinessException.class, () -> controller.getProfile(authentication));
    }
}

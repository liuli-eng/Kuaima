package com.kuaima.app.controller.boss;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.coupon.service.BossCouponService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class BossCouponControllerTests {
    @Test
    void usesBossIdFromJwt() {
        BossCouponService service = mock(BossCouponService.class);
        BossCouponController controller = new BossCouponController(service);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(7L, "boss", UserRole.BOSS));
        when(service.list(7L, "UNUSED")).thenReturn(List.of());
        assertTrue(controller.list("UNUSED", authentication).getData().isEmpty());
        verify(service).list(7L, "UNUSED");
    }

    @Test
    void rejectsWorkerJwt() {
        BossCouponController controller = new BossCouponController(mock(BossCouponService.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(7L, "worker", UserRole.USER));
        assertThrows(ForbiddenBusinessException.class, () -> controller.list(null, authentication));
    }
}

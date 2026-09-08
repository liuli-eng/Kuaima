package com.kuaima.app.controller.wallet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.wallet.service.SettlementService;
import com.kuaima.app.security.model.LoginUser;

class BossSettlementControllerTests {

    @Test
    void pending_shouldRejectNonBossIdentity() {
        SettlementService service = mock(SettlementService.class);
        BossSettlementController controller = new BossSettlementController(service);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(2L, "worker", UserRole.USER));

        assertThrows(IllegalStateException.class, () -> controller.listPending(authentication));
    }
}

package com.kuaima.app.controller.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.reward.model.WorkerRewardModels.WithdrawalRequest;
import com.kuaima.app.domain.reward.service.WorkerRewardService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

class WorkerRewardControllerTests {
    @Test
    void overviewUsesWorkerIdFromJwt() {
        WorkerRewardService service = mock(WorkerRewardService.class);
        WorkerRewardController controller = new WorkerRewardController(service);
        Authentication authentication = mock(Authentication.class);
        Map<String, Object> overview = Map.of("balance", 2000L);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(30L, "worker", UserRole.USER));
        when(service.overview(30L)).thenReturn(overview);

        assertSameOverview(controller, authentication, service, overview);
    }

    @Test
    void bossJwtCannotAccessWorkerRewards() {
        WorkerRewardController controller = new WorkerRewardController(mock(WorkerRewardService.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(30L, "boss", UserRole.BOSS));

        assertThrows(ForbiddenBusinessException.class, () -> controller.overview(authentication));
    }

    @Test
    void withdrawUsesBodyAndIdempotencyHeaderWithoutUserId() {
        WorkerRewardService service = mock(WorkerRewardService.class);
        WorkerRewardController controller = new WorkerRewardController(service);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(30L, "worker", UserRole.USER));
        Map<String, Object> withdrawal = Map.of("withdrawId", 90001L);
        when(service.withdraw(30L, 1000L, "WECHAT", "request-1")).thenReturn(withdrawal);

        var result = controller.withdraw(new WithdrawalRequest(1000L, "WECHAT"), "request-1", authentication);

        assertEquals(200, result.getCode());
        assertEquals("提现申请已提交", result.getMessage());
        assertEquals(withdrawal, result.getData());
        verify(service).withdraw(30L, 1000L, "WECHAT", "request-1");
    }

    private void assertSameOverview(WorkerRewardController controller, Authentication authentication,
            WorkerRewardService service, Map<String, Object> overview) {
        var result = controller.overview(authentication);
        assertEquals(overview, result.getData());
        verify(service).overview(30L);
    }
}

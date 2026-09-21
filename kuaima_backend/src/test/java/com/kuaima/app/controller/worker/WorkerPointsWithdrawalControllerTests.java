package com.kuaima.app.controller.worker;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.points.service.WorkerPointsWithdrawalService;
import com.kuaima.app.security.model.LoginUser;

class WorkerPointsWithdrawalControllerTests {
    @Test
    void bossIdentityCannotAccessWorkerWithdrawalConfig() {
        var controller = new WorkerPointsWithdrawalController(mock(WorkerPointsWithdrawalService.class));
        var authentication = new UsernamePasswordAuthenticationToken(new LoginUser(7L, "boss", "BOSS"), null);
        assertThrows(ForbiddenBusinessException.class, () -> controller.config(authentication));
    }
}

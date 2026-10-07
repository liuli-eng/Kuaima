package com.kuaima.app.controller.learn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.security.model.LoginUser;

class WorkerClassroomControllerTests {
    @Test
    void overviewDoesNotRequireAdministratorAuthentication() {
        WorkerClassroomService service = mock(WorkerClassroomService.class);
        when(service.overview()).thenReturn(Map.of("simulateVideos", java.util.List.of()));
        WorkerClassroomController controller = new WorkerClassroomController(service);

        assertEquals(200, controller.overview().getCode());
    }

    @Test
    void quizRequiresUserRole() {
        WorkerClassroomController controller = new WorkerClassroomController(mock(WorkerClassroomService.class));
        Authentication boss = new UsernamePasswordAuthenticationToken(new LoginUser(1L, "boss", "BOSS"), null);
        assertThrows(ForbiddenBusinessException.class, () -> controller.quiz(boss));
    }

    @Test
    void quizAllowsNormalUserRole() {
        WorkerClassroomService service = mock(WorkerClassroomService.class);
        org.mockito.Mockito.when(service.quiz()).thenReturn(Map.of("questions", java.util.List.of(), "passScore", 60));
        WorkerClassroomController controller = new WorkerClassroomController(service);
        Authentication user = new UsernamePasswordAuthenticationToken(new LoginUser(2L, "worker", "USER"), null);
        assertEquals(200, controller.quiz(user).getCode());
    }
}

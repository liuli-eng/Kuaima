package com.kuaima.app.controller.talent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.talent.repository.TalentFavoriteRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

class TalentControllerInviteTests {

    @Test
    void inviteWorker_shouldAcceptStringIdsAndUseCurrentBoss() {
        UserRepository userRepository = mock(UserRepository.class);
        MessageService messageService = mock(MessageService.class);
        TalentController controller = controller(userRepository, messageService);
        User boss = new User();
        boss.setId(1L);
        boss.setCompanyName("测试企业");
        when(userRepository.findById(1L)).thenReturn(Optional.of(boss));
        Map<String, Object> body = new HashMap<>();
        body.put("bossId", "1");
        body.put("workerId", "2");
        body.put("orderId", "36");

        var result = controller.inviteWorker(body, bossAuthentication(1L));

        assertEquals(true, result.getData().get("invited"));
        verify(messageService).sendToUser(2L, UserRole.USER, "BOSS_INVITE", "招聘邀请",
                "测试企业 邀请您加入他们的岗位，快去看看吧！", BizType.ORDER, 36L);
    }

    @Test
    void inviteWorker_shouldAlsoAcceptJsonNumberValues() {
        UserRepository userRepository = mock(UserRepository.class);
        MessageService messageService = mock(MessageService.class);
        TalentController controller = controller(userRepository, messageService);
        Map<String, Object> body = new HashMap<>();
        body.put("bossId", Integer.valueOf(1));
        body.put("workerId", Integer.valueOf(2));
        body.put("orderId", Integer.valueOf(36));

        controller.inviteWorker(body, bossAuthentication(1L));

        verify(messageService).sendToUser(2L, UserRole.USER, "BOSS_INVITE", "招聘邀请",
                "老板 邀请您加入他们的岗位，快去看看吧！", BizType.ORDER, 36L);
    }

    @Test
    void inviteWorker_shouldRejectAnotherBossId() {
        TalentController controller = controller(mock(UserRepository.class), mock(MessageService.class));
        Map<String, Object> body = Map.of("bossId", "9", "workerId", "2", "orderId", "36");

        assertThrows(ForbiddenBusinessException.class,
                () -> controller.inviteWorker(body, bossAuthentication(1L)));
    }

    @Test
    void inviteWorker_shouldRejectNonNumericIdsAsBadRequest() {
        TalentController controller = controller(mock(UserRepository.class), mock(MessageService.class));
        Map<String, Object> body = Map.of("bossId", "1", "workerId", "worker-x");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> controller.inviteWorker(body, bossAuthentication(1L)));

        assertEquals("workerId 必须是整数", error.getMessage());
    }

    private TalentController controller(UserRepository userRepository, MessageService messageService) {
        return new TalentController(userRepository, mock(TalentFavoriteRepository.class),
                mock(BaseOrderItemRespository.class), messageService);
    }

    private Authentication bossAuthentication(Long id) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(id, "boss", UserRole.BOSS));
        return authentication;
    }
}

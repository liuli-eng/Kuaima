package com.kuaima.app.controller.talent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.talent.repository.TalentFavoriteRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

class TalentControllerHistoryTests {

    @Test
    void listHistory_shouldUseCurrentBossAndKeepLatestWorkerOrder() {
        UserRepository userRepository = mock(UserRepository.class);
        BaseOrderItemRespository itemRepository = mock(BaseOrderItemRespository.class);
        TalentController controller = new TalentController(userRepository,
                mock(TalentFavoriteRepository.class), itemRepository, mock(MessageService.class));
        Authentication authentication = bossAuthentication(1L);

        BaseOrderItem latestWorkerTwo = item(103L, 2L);
        BaseOrderItem workerOne = item(102L, 1L);
        BaseOrderItem olderWorkerTwo = item(101L, 2L);
        when(itemRepository.findHistoryByBossId(1L))
                .thenReturn(List.of(latestWorkerTwo, workerOne, olderWorkerTwo));
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user(1L)));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(user(2L)));

        var result = controller.listHistory(1L, authentication);

        assertEquals(List.of(2L, 1L), result.getData().stream().map(User::getId).toList());
        verify(itemRepository).findHistoryByBossId(1L);
    }

    @Test
    void listHistory_shouldAllowOmittedCompatibilityBossId() {
        BaseOrderItemRespository itemRepository = mock(BaseOrderItemRespository.class);
        TalentController controller = new TalentController(mock(UserRepository.class),
                mock(TalentFavoriteRepository.class), itemRepository, mock(MessageService.class));
        when(itemRepository.findHistoryByBossId(1L)).thenReturn(List.of());

        var result = controller.listHistory(null, bossAuthentication(1L));

        assertEquals(List.of(), result.getData());
        verify(itemRepository).findHistoryByBossId(1L);
    }

    @Test
    void listHistory_shouldRejectAnotherBossId() {
        TalentController controller = new TalentController(mock(UserRepository.class),
                mock(TalentFavoriteRepository.class), mock(BaseOrderItemRespository.class),
                mock(MessageService.class));

        assertThrows(ForbiddenBusinessException.class,
                () -> controller.listHistory(2L, bossAuthentication(1L)));
    }

    @Test
    void listHistory_shouldRejectWorkerIdentity() {
        TalentController controller = new TalentController(mock(UserRepository.class),
                mock(TalentFavoriteRepository.class), mock(BaseOrderItemRespository.class),
                mock(MessageService.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(1L, "worker", UserRole.USER));

        assertThrows(ForbiddenBusinessException.class,
                () -> controller.listHistory(1L, authentication));
    }

    private Authentication bossAuthentication(Long id) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(id, "boss", UserRole.BOSS));
        return authentication;
    }

    private BaseOrderItem item(Long id, Long userId) {
        BaseOrderItem item = new BaseOrderItem();
        item.setId(id);
        item.setUserId(userId);
        return item;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}

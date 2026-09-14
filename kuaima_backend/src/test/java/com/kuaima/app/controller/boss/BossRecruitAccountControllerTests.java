package com.kuaima.app.controller.boss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.entity.BossRecruitAccount;
import com.kuaima.app.domain.boss.repository.BossRecruitAccountRepository;
import com.kuaima.app.security.model.LoginUser;

class BossRecruitAccountControllerTests {
    @Test
    void switchingAccount_shouldLeaveExactlyOneCurrentAccount() {
        BossRecruitAccountRepository repository = mock(BossRecruitAccountRepository.class);
        BossRecruitAccountController controller = new BossRecruitAccountController(repository);
        BossRecruitAccount first = account(1L, 7L, true);
        BossRecruitAccount second = account(2L, 7L, false);
        when(repository.findByIdAndOwnerUserId(2L, 7L)).thenReturn(Optional.of(second));
        when(repository.findByOwnerUserIdOrderByIdAsc(7L)).thenReturn(List.of(first, second));

        controller.current(Map.of("accountId", 2L), bossAuthentication(7L));

        assertFalse(first.getCurrent());
        assertTrue(second.getCurrent());
        verify(repository).saveAll(anyList());
    }

    @Test
    void switchingAccount_shouldRejectAnotherBossAccount() {
        BossRecruitAccountRepository repository = mock(BossRecruitAccountRepository.class);
        BossRecruitAccountController controller = new BossRecruitAccountController(repository);
        when(repository.findByIdAndOwnerUserId(9L, 7L)).thenReturn(Optional.empty());
        assertThrows(ForbiddenBusinessException.class,
                () -> controller.current(Map.of("accountId", 9L), bossAuthentication(7L)));
    }

    private Authentication bossAuthentication(Long id) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(id, "boss", "BOSS"));
        return authentication;
    }

    private BossRecruitAccount account(Long id, Long ownerId, boolean current) {
        BossRecruitAccount account = new BossRecruitAccount(); account.setId(id); account.setOwnerUserId(ownerId);
        account.setCurrent(current); account.setName("账号" + id); return account;
    }
}

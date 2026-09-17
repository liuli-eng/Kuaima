package com.kuaima.app.controller.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;

import com.kuaima.app.admin.service.AdminLogService;
import com.kuaima.app.common.BusinessHttpException;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossRecruitAccount;
import com.kuaima.app.domain.boss.model.BossProfileModels.AddAccountRequest;
import com.kuaima.app.domain.boss.model.BossProfileModels.AddAccountResponse;
import com.kuaima.app.domain.boss.model.BossProfileModels.AddedAccountView;
import com.kuaima.app.domain.boss.repository.BossRecruitAccountRepository;
import com.kuaima.app.domain.boss.service.BossRecruitAccountService;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

class BossRecruitAccountControllerTests {
    @Test
    void switchingAccount_shouldLeaveExactlyOneCurrentAccount() {
        BossRecruitAccountRepository repository = mock(BossRecruitAccountRepository.class);
        BossRecruitAccountController controller = controller(repository);
        BossRecruitAccount first = account(1L, 7L, true);
        BossRecruitAccount second = account(2L, 7L, false);
        when(repository.findByIdAndOwnerUserId(2L, 7L)).thenReturn(Optional.of(second));
        when(repository.findByOwnerUserIdOrderByIdAsc(7L)).thenReturn(List.of(first, second));

        controller.current(Map.of("accountId", 2L), bossAuthentication(7L), new MockHttpServletRequest());

        assertFalse(first.getCurrent());
        assertTrue(second.getCurrent());
        verify(repository).saveAll(anyList());
    }

    @Test
    void switchingAccount_shouldAcceptJacksonSmallIntegerAccountId() {
        BossRecruitAccountRepository repository = mock(BossRecruitAccountRepository.class);
        BossRecruitAccountController controller = controller(repository);
        BossRecruitAccount selected = account(2L, 7L, false);
        when(repository.findByIdAndOwnerUserId(2L, 7L)).thenReturn(Optional.of(selected));
        when(repository.findByOwnerUserIdOrderByIdAsc(7L)).thenReturn(List.of(selected));

        controller.current(Map.of("accountId", 2), bossAuthentication(7L), new MockHttpServletRequest());

        assertTrue(selected.getCurrent());
        verify(repository).saveAll(anyList());
    }

    @Test
    void switchingAccount_shouldRejectAnotherBossAccount() {
        BossRecruitAccountRepository repository = mock(BossRecruitAccountRepository.class);
        BossRecruitAccountController controller = controller(repository);
        when(repository.findByIdAndOwnerUserId(9L, 7L)).thenReturn(Optional.empty());
        assertThrows(ForbiddenBusinessException.class, () -> controller.current(
                Map.of("accountId", 9L), bossAuthentication(7L), new MockHttpServletRequest()));
    }

    @Test
    void addingAccount_shouldReturnNestedResponseAndWriteAuditLog() {
        BossRecruitAccountRepository repository = mock(BossRecruitAccountRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BossRecruitAccountService accountService = mock(BossRecruitAccountService.class);
        AdminLogService logService = mock(AdminLogService.class);
        BossRecruitAccountController controller = new BossRecruitAccountController(
                repository, userRepository, accountService, logService);
        AddedAccountView account = new AddedAccountView(5L, "企业管理员", "", "139****1234",
                "ENTERPRISE", false, false);
        AddAccountResponse response = new AddAccountResponse(account, 1L);
        AddAccountRequest request = new AddAccountRequest("13900131234", "123456", true);
        when(accountService.add(7L, request)).thenReturn(response);

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("192.0.2.1");
        Result<AddAccountResponse> result = controller.add(request, bossAuthentication(7L), httpRequest);

        assertEquals(200, result.getCode());
        assertEquals("账号添加成功", result.getMessage());
        assertEquals(5L, result.getData().account().id());
        assertEquals(1L, result.getData().currentAccountId());
        verify(logService).record(eq("boss"), eq(7L), eq("添加招聘账号"), eq("账号=5"),
                eq("192.0.2.1"), eq("成功"), eq("手机号=139****1234"));
    }

    @Test
    void addingAccount_shouldRejectTooManyRequests() {
        BossRecruitAccountRepository repository = mock(BossRecruitAccountRepository.class);
        BossRecruitAccountService accountService = mock(BossRecruitAccountService.class);
        BossRecruitAccountController controller = new BossRecruitAccountController(repository,
                mock(UserRepository.class), accountService, mock(AdminLogService.class));
        Authentication authentication = bossAuthentication(7L);
        when(accountService.add(any(Long.class), any())).thenReturn(new AddAccountResponse(
                new AddedAccountView(5L, "账号", "", "139****1234", "PERSONAL", false, false), 1L));

        BusinessHttpException error = assertThrows(BusinessHttpException.class, () -> {
            controller.add(new AddAccountRequest("13900131234", "123456", true), authentication,
                    new MockHttpServletRequest());
            controller.add(new AddAccountRequest("13900131234", "123456", true), authentication,
                    new MockHttpServletRequest());
        });

        assertEquals(429, error.getStatus().value());
    }

    private Authentication bossAuthentication(Long id) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(id, "boss", "BOSS"));
        return authentication;
    }

    private BossRecruitAccount account(Long id, Long ownerId, boolean current) {
        BossRecruitAccount account = new BossRecruitAccount();
        account.setId(id);
        account.setOwnerUserId(ownerId);
        account.setTargetUserId(ownerId);
        account.setCurrent(current);
        account.setName("账号" + id);
        return account;
    }

    private BossRecruitAccountController controller(BossRecruitAccountRepository repository) {
        return new BossRecruitAccountController(repository, mock(UserRepository.class),
                mock(BossRecruitAccountService.class), mock(AdminLogService.class));
    }
}

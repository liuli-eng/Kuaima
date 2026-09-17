package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kuaima.app.common.BusinessHttpException;
import com.kuaima.app.domain.boss.entity.BossRecruitAccount;
import com.kuaima.app.domain.boss.model.BossProfileModels.AddAccountRequest;
import com.kuaima.app.domain.boss.model.BossProfileModels.AddAccountResponse;
import com.kuaima.app.domain.boss.repository.BossRecruitAccountRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.service.SmsService;

class BossRecruitAccountServiceTests {
    private final BossRecruitAccountRepository accountRepository = mock(BossRecruitAccountRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SmsService smsService = mock(SmsService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final BossRecruitAccountService service = new BossRecruitAccountService(
            accountRepository, userRepository, smsService, passwordEncoder);

    @Test
    void add_shouldBindExistingBossAndKeepCurrentAccountUnchanged() {
        User owner = user(1L, UserRole.BOSS, "13800138000");
        User target = user(2L, UserRole.BOSS, "13900131234");
        target.setCompanyName("企业管理员");
        target.setEnterpriseStatus("APPROVED");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findByPhone("13900131234")).thenReturn(List.of(target));
        when(accountRepository.existsByOwnerUserIdAndTargetUserId(1L, 2L)).thenReturn(false);
        when(smsService.verifyCodeResult("13900131234", "123456"))
                .thenReturn(SmsService.VerifyResult.OK);
        when(accountRepository.saveAndFlush(any(BossRecruitAccount.class))).thenAnswer(invocation -> {
            BossRecruitAccount account = invocation.getArgument(0);
            account.setId(5L);
            return account;
        });
        when(accountRepository.findByOwnerUserIdAndCurrentTrue(1L))
                .thenReturn(Optional.of(binding(1L, 1L, true)));

        AddAccountResponse response = service.add(1L, request("13900131234", true));

        assertEquals(5L, response.account().id());
        assertEquals("企业管理员", response.account().name());
        assertEquals("139****1234", response.account().phone());
        assertEquals("ENTERPRISE", response.account().authorizationType());
        assertFalse(response.account().current());
        assertFalse(response.account().newlyRegistered());
        assertEquals(1L, response.currentAccountId());
        verify(smsService).verifyCodeResult("13900131234", "123456");
    }

    @Test
    void add_shouldCreateBossAndDefaultAccountForUnregisteredPhone() {
        User owner = user(1L, UserRole.BOSS, "13800138000");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findByPhone("13900131234")).thenReturn(List.of());
        when(smsService.verifyCodeResult("13900131234", "123456"))
                .thenReturn(SmsService.VerifyResult.OK);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(accountRepository.saveAndFlush(any(BossRecruitAccount.class))).thenAnswer(invocation -> {
            BossRecruitAccount account = invocation.getArgument(0);
            account.setId(5L);
            return account;
        });
        when(accountRepository.findByOwnerUserIdAndCurrentTrue(1L))
                .thenReturn(Optional.of(binding(1L, 1L, true)));

        AddAccountResponse response = service.add(1L, request("13900131234", true));

        assertTrue(response.account().newlyRegistered());
        assertEquals("139****1234", response.account().phone());
        verify(userRepository).saveAndFlush(any(User.class));
        verify(accountRepository).save(any(BossRecruitAccount.class));
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void add_shouldExplicitlyRejectWorkerOnlyPhone() {
        User owner = user(1L, UserRole.BOSS, "13800138000");
        User worker = user(2L, UserRole.USER, "13900131234");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findByPhone("13900131234")).thenReturn(List.of(worker));

        BusinessHttpException error = assertThrows(BusinessHttpException.class,
                () -> service.add(1L, request("13900131234", true)));

        assertEquals(403, error.getStatus().value());
        verify(smsService, never()).verifyCodeResult(anyString(), anyString());
    }

    @Test
    void add_shouldRejectCurrentAndDuplicatedAccounts() {
        User owner = user(1L, UserRole.BOSS, "13800138000");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));

        BusinessHttpException current = assertThrows(BusinessHttpException.class,
                () -> service.add(1L, request("13800138000", true)));
        assertEquals(409, current.getStatus().value());

        User target = user(2L, UserRole.BOSS, "13900131234");
        when(userRepository.findByPhone("13900131234")).thenReturn(List.of(target));
        when(accountRepository.existsByOwnerUserIdAndTargetUserId(1L, 2L)).thenReturn(true);
        BusinessHttpException duplicated = assertThrows(BusinessHttpException.class,
                () -> service.add(1L, request("13900131234", true)));
        assertEquals(409, duplicated.getStatus().value());
    }

    @Test
    void add_shouldValidatePhoneCodeAndAgreement() {
        assertEquals("手机号格式错误", validationMessage(new AddAccountRequest("12800138000", "123456", true)));
        assertEquals("短信验证码格式错误", validationMessage(new AddAccountRequest("13900131234", "abc123", true)));
        assertEquals("未同意用户协议和隐私协议", validationMessage(new AddAccountRequest("13900131234", "123456", false)));
    }

    @Test
    void add_shouldMapMissingInvalidAndUsedVerificationCodes() {
        User owner = user(1L, UserRole.BOSS, "13800138000");
        User target = user(2L, UserRole.BOSS, "13900131234");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findByPhone("13900131234")).thenReturn(List.of(target));
        when(accountRepository.existsByOwnerUserIdAndTargetUserId(1L, 2L)).thenReturn(false);

        when(smsService.verifyCodeResult("13900131234", "123456"))
                .thenReturn(SmsService.VerifyResult.NOT_FOUND);
        assertEquals(404, verificationError().getStatus().value());

        when(smsService.verifyCodeResult("13900131234", "123456"))
                .thenReturn(SmsService.VerifyResult.INVALID);
        assertEquals(422, verificationError().getStatus().value());

        when(smsService.verifyCodeResult("13900131234", "123456"))
                .thenReturn(SmsService.VerifyResult.EXPIRED_OR_USED);
        assertEquals(422, verificationError().getStatus().value());
    }

    @Test
    void add_shouldConvertConcurrentUniqueViolationToConflict() {
        User owner = user(1L, UserRole.BOSS, "13800138000");
        User target = user(2L, UserRole.BOSS, "13900131234");
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findByPhone("13900131234")).thenReturn(List.of(target));
        when(accountRepository.existsByOwnerUserIdAndTargetUserId(1L, 2L)).thenReturn(false);
        when(smsService.verifyCodeResult("13900131234", "123456"))
                .thenReturn(SmsService.VerifyResult.OK);
        when(accountRepository.saveAndFlush(any(BossRecruitAccount.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));

        BusinessHttpException error = assertThrows(BusinessHttpException.class,
                () -> service.add(1L, request("13900131234", true)));

        assertEquals(409, error.getStatus().value());
    }

    private BusinessHttpException verificationError() {
        return assertThrows(BusinessHttpException.class, () -> service.add(1L, request("13900131234", true)));
    }

    private String validationMessage(AddAccountRequest request) {
        return assertThrows(IllegalArgumentException.class, () -> service.add(1L, request)).getMessage();
    }

    private AddAccountRequest request(String phone, boolean agreed) {
        return new AddAccountRequest(phone, "123456", agreed);
    }

    private User user(Long id, String role, String phone) {
        User user = new User();
        user.setId(id);
        user.setUsername("user-" + id);
        user.setRole(role);
        user.setPhone(phone);
        user.setNickname("用户" + id);
        return user;
    }

    private BossRecruitAccount binding(Long id, Long targetUserId, boolean current) {
        BossRecruitAccount account = new BossRecruitAccount();
        account.setId(id);
        account.setOwnerUserId(1L);
        account.setTargetUserId(targetUserId);
        account.setCurrent(current);
        account.setName("账号" + id);
        return account;
    }
}

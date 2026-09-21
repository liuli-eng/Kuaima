package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.kuaima.app.domain.boss.entity.BossAttendanceCode;
import com.kuaima.app.domain.boss.model.BossRecruitSettingsModels.Settings;
import com.kuaima.app.domain.boss.repository.BossAttendanceCodeRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.enterprise.entity.Enterprise;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;
import jakarta.persistence.EntityNotFoundException;

class BossAttendanceCodeServiceTests {
    @Test
    void dailyCodeMethods_shouldUseIndependentTransactionToAvoidConcurrentDuplicateInsert() throws Exception {
        for (var method : BossAttendanceCodeService.class.getDeclaredMethods()) {
            if (!java.util.Set.of("today", "refresh", "verify").contains(method.getName())) continue;
            Transactional transactional = method.getAnnotation(Transactional.class);
            assertNotNull(transactional);
            assertEquals(Propagation.REQUIRES_NEW, transactional.propagation());
        }
    }

    @Test
    void todayShouldLockBossBeforeFindOrCreateAndReuseExistingRow() {
        BossAttendanceCodeRepository codes = mock(BossAttendanceCodeRepository.class);
        BossRecruitSettingsService settings = mock(BossRecruitSettingsService.class);
        UserRepository users = mock(UserRepository.class);
        User boss = new User(); boss.setId(1L);
        BossAttendanceCode existing = new BossAttendanceCode(); existing.setId(6L); existing.setBossId(1L);
        existing.setCodeDate(LocalDate.now()); existing.setWorkCode("1020"); existing.setLeaveCode("7665");
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(boss));
        when(codes.findByBossIdAndCodeDate(1L, LocalDate.now())).thenReturn(Optional.of(existing));
        when(settings.get(1L)).thenReturn(view(true, true));
        when(codes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new BossAttendanceCodeService(codes, settings, users).today(1L);

        assertEquals("1020", result.get("workCode"));
        verify(users).findByIdForUpdate(1L);
        verify(codes).save(existing);
    }

    @Test
    void missingBossShouldFailBeforeCreatingAttendanceCode() {
        BossAttendanceCodeRepository codes = mock(BossAttendanceCodeRepository.class);
        BossRecruitSettingsService settings = mock(BossRecruitSettingsService.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findByIdForUpdate(9L)).thenReturn(Optional.empty());
        var service = new BossAttendanceCodeService(codes, settings, users);

        assertThrows(EntityNotFoundException.class, () -> service.today(9L));
        verifyNoInteractions(codes, settings);
    }

    @Test
    void enterpriseRefreshShouldReuseLegacyBossRowBeforeInsert() {
        BossAttendanceCodeRepository codes = mock(BossAttendanceCodeRepository.class);
        BossRecruitSettingsService settings = mock(BossRecruitSettingsService.class);
        UserRepository users = mock(UserRepository.class);
        EnterpriseRepository enterprises = mock(EnterpriseRepository.class);
        User boss = new User(); boss.setId(1L); boss.setPhone("13900000000");
        Enterprise enterprise = new Enterprise(); enterprise.setId(4L); enterprise.setStatus("ACTIVE");
        BossAttendanceCode legacy = new BossAttendanceCode(); legacy.setId(6L); legacy.setBossId(1L);
        legacy.setCodeDate(LocalDate.now()); legacy.setWorkCode("1020");
        when(enterprises.findByIdForUpdate(4L)).thenReturn(Optional.of(enterprise));
        when(codes.findByEnterpriseIdAndCodeDate(4L, LocalDate.now())).thenReturn(Optional.empty());
        when(codes.findByBossIdAndCodeDate(1L, LocalDate.now())).thenReturn(Optional.of(legacy));
        when(users.findById(1L)).thenReturn(Optional.of(boss));
        when(settings.getByEnterprise(eq(4L), eq("13900000000"))).thenReturn(view(true, true));
        when(codes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new BossAttendanceCodeService(codes, settings, users, enterprises).refreshByEnterprise(4L, 1L, false);

        assertEquals("1020", result.get("workCode"));
        assertEquals(4L, legacy.getEnterpriseId());
        verify(codes, times(2)).save(legacy);
    }

    private Settings view(boolean start, boolean early) {
        return new Settings("零工需打电话", null, "开工后3小时", false, start, early,
                "按工作设定时间", true, "日结", "daily", "auto", true, true, true, true);
    }
}

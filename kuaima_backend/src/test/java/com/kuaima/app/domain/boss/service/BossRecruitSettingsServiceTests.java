package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.kuaima.app.domain.boss.model.BossRecruitSettingsModels.Settings;
import com.kuaima.app.domain.boss.repository.BossRecruitSettingsRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;

class BossRecruitSettingsServiceTests {
    @Test
    void firstQuery_shouldUseCurrentBossRealPhone() {
        BossRecruitSettingsRepository settings = mock(BossRecruitSettingsRepository.class);
        UserRepository users = mock(UserRepository.class);
        User boss = new User(); boss.setPhone("13900000000");
        when(settings.findByBossId(7L)).thenReturn(Optional.empty());
        when(users.findById(7L)).thenReturn(Optional.of(boss));

        assertEquals("13900000000", new BossRecruitSettingsService(settings, users).get(7L).backupPhone());
    }

    @Test
    void firstQuery_shouldReturnEmptyPhoneWhenBossHasNoPhone() {
        BossRecruitSettingsRepository settings = mock(BossRecruitSettingsRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(settings.findByBossId(7L)).thenReturn(Optional.empty());
        when(users.findById(7L)).thenReturn(Optional.empty());

        assertEquals("", new BossRecruitSettingsService(settings, users).get(7L).backupPhone());
    }

    @Test
    void save_shouldAllowEmptyBackupPhoneButRejectInvalidPhone() {
        BossRecruitSettingsService service = new BossRecruitSettingsService(mock(BossRecruitSettingsRepository.class));
        Settings valid = service.defaults();
        Settings invalid = new Settings(valid.phoneMode(), "123", valid.stopTime(), valid.dndEnabled(),
                valid.startCodeEnabled(), valid.earlyCodeEnabled(), valid.attendanceMode(), valid.faceClockEnabled(),
                valid.settleMode(), valid.type(), valid.signMode(), valid.phoneNotify(), valid.signNotify(),
                valid.startRemind(), valid.settleNotify());

        assertThrows(IllegalArgumentException.class, () -> service.save(7L, invalid));
    }
}

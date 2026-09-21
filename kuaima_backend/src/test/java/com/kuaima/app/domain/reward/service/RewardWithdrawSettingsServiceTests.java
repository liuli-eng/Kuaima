package com.kuaima.app.domain.reward.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.admin.entity.AdminSetting;
import com.kuaima.app.admin.repository.AdminSettingRepository;

class RewardWithdrawSettingsServiceTests {
    private AdminSettingRepository settings;
    private RewardWithdrawSettingsService service;

    @BeforeEach
    void setUp() {
        settings = mock(AdminSettingRepository.class);
        service = new RewardWithdrawSettingsService(settings);
    }

    @Test
    void readsMinimumSwitchAndChannelFromAdminSettings() {
        setting(RewardWithdrawSettingsService.MINIMUM_AMOUNT_KEY, "1500");
        setting(RewardWithdrawSettingsService.ENABLED_KEY, "false");
        setting(RewardWithdrawSettingsService.DISABLED_REASON_KEY, "系统维护");
        setting(RewardWithdrawSettingsService.CHANNEL_KEY, "wechat");

        var result = service.settings();

        assertEquals(1500L, result.minimumAmount());
        assertEquals(false, result.enabled());
        assertEquals("系统维护", result.disabledReason());
        assertEquals("WECHAT", result.channel());
    }

    @Test
    void missingMinimumSettingIsRejectedInsteadOfFallingBackToCode() {
        assertThrows(IllegalStateException.class, () -> service.settings());
    }

    @Test
    void invalidMinimumSettingIsRejected() {
        setting(RewardWithdrawSettingsService.MINIMUM_AMOUNT_KEY, "10.00");

        assertThrows(IllegalStateException.class, () -> service.settings());
    }

    private void setting(String key, String value) {
        AdminSetting setting = new AdminSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        when(settings.findById(key)).thenReturn(Optional.of(setting));
    }
}

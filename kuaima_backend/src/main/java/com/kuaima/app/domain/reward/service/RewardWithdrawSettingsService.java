package com.kuaima.app.domain.reward.service;

import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kuaima.app.admin.entity.AdminSetting;
import com.kuaima.app.admin.repository.AdminSettingRepository;

/** 奖励金提现配置读取服务。最低金额单位为“分”，来源为后台系统配置。 */
@Service
public class RewardWithdrawSettingsService {
    public static final String MINIMUM_AMOUNT_KEY = "reward.withdraw.minimumAmount";
    public static final String ENABLED_KEY = "reward.withdraw.enabled";
    public static final String DISABLED_REASON_KEY = "reward.withdraw.disabledReason";
    public static final String CHANNEL_KEY = "reward.withdraw.channel";

    private final AdminSettingRepository settings;

    public RewardWithdrawSettingsService(AdminSettingRepository settings) {
        this.settings = settings;
    }

    public RewardWithdrawSettings settings() {
        long minimumAmount = requiredLong(MINIMUM_AMOUNT_KEY, value -> {
            try {
                long parsed = Long.parseLong(value);
                if (parsed <= 0) throw new IllegalArgumentException("必须大于0");
                return parsed;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("必须是整数");
            }
        });
        boolean enabled = optional(ENABLED_KEY, "true", Boolean::parseBoolean);
        String channel = optional(CHANNEL_KEY, "WECHAT", value -> value.trim().toUpperCase());
        String disabledReason = settings.findById(DISABLED_REASON_KEY)
                .map(AdminSetting::getSettingValue)
                .filter(StringUtils::hasText)
                .orElse(null);
        return new RewardWithdrawSettings(minimumAmount, enabled, disabledReason, channel);
    }

    private <T> T requiredLong(String key, Function<String, T> parser) {
        String value = settings.findById(key).map(AdminSetting::getSettingValue).filter(StringUtils::hasText).orElse(null);
        if (value == null) throw new IllegalStateException("奖励金提现最低金额配置缺失");
        try {
            return parser.apply(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("奖励金提现最低金额配置无效: " + key);
        }
    }

    private <T> T optional(String key, String defaultValue, Function<String, T> parser) {
        return settings.findById(key)
                .map(AdminSetting::getSettingValue)
                .filter(StringUtils::hasText)
                .map(value -> parser.apply(value.trim()))
                .orElse(parser.apply(defaultValue));
    }

    public record RewardWithdrawSettings(
            long minimumAmount,
            boolean enabled,
            String disabledReason,
            String channel) {
    }
}

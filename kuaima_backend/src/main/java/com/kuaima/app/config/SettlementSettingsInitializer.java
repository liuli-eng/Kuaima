package com.kuaima.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.kuaima.app.admin.entity.AdminSetting;
import com.kuaima.app.admin.repository.AdminSettingRepository;

/** 保证后台展示的默认结算规则与后端实际计算规则一致。 */
@Component
@Order(110)
public class SettlementSettingsInitializer implements CommandLineRunner {
    private final AdminSettingRepository settings;

    public SettlementSettingsInitializer(AdminSettingRepository settings) {
        this.settings = settings;
    }

    @Override
    public void run(String... args) {
        seed("rules.platformFeeEnabled", "true", "平台服务费");
        seed("rules.feeRate", "5", "服务费比例");
    }

    private void seed(String key, String value, String description) {
        if (settings.existsById(key)) return;
        AdminSetting setting = new AdminSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setCategory("rules");
        setting.setDescription(description);
        settings.save(setting);
    }
}

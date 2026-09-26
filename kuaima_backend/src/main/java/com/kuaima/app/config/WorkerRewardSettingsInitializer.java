package com.kuaima.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.kuaima.app.admin.entity.AdminSetting;
import com.kuaima.app.admin.repository.AdminSettingRepository;
import com.kuaima.app.domain.reward.entity.RewardEarningRule;
import com.kuaima.app.domain.reward.repository.RewardEarningRuleRepository;

/** 初始化零工端奖励金提现配置和获取规则；不覆盖运营人员已修改的数据。 */
@Component
@Order(120)
public class WorkerRewardSettingsInitializer implements CommandLineRunner {
    private final AdminSettingRepository settings;
    private final RewardEarningRuleRepository rules;

    public WorkerRewardSettingsInitializer(AdminSettingRepository settings, RewardEarningRuleRepository rules) {
        this.settings = settings;
        this.rules = rules;
    }

    @Override
    public void run(String... args) {
        // 测试阶段不设置固定门槛，提现金额只需大于 0 分；正式运营前再由后台配置业务最低金额。
        setting("reward.withdraw.minimumAmount", "1", "奖励金最低提现金额（分），测试阶段为1分");
        setting("reward.withdraw.enabled", "true", "奖励金提现开关");
        setting("reward.withdraw.disabledReason", "", "奖励金提现关闭原因");
        setting("reward.withdraw.channel", "WECHAT", "奖励金提现渠道");
        setting("reward.recharge.quickAmounts", "0.01:0,1000:30,2000:80,5000:260,10000:600,20000:1400",
                "奖励金充值快捷金额与赠送金额，格式为本金:赠送金");
        rule("INVITE_FIRST_ORDER", "邀请好友接单", "好友注册并完成首单后发放", 800L,
                "NAVIGATE", "/pages/worker/invite", 10);
        rule("INCOME_SHARE", "晒收入分享", "分享收入海报可获得奖励", 200L,
                "SHARE", null, 20);
        rule("ACTIVITY", "活动奖励", "参与平台活动赢取奖励金", null,
                "NONE", null, 30);
    }

    private void setting(String key, String value, String description) {
        if (settings.existsById(key)) return;
        AdminSetting setting = new AdminSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setCategory("reward");
        setting.setDescription(description);
        settings.save(setting);
    }

    private void rule(String code, String name, String description, Long rewardAmount,
            String actionType, String actionPath, int sort) {
        if (rules.findByCode(code).isPresent()) return;
        RewardEarningRule rule = new RewardEarningRule();
        rule.setCode(code);
        rule.setName(name);
        rule.setDescription(description);
        rule.setRewardAmount(rewardAmount);
        rule.setActionType(actionType);
        rule.setActionPath(actionPath);
        rule.setEnabled(true);
        rule.setSort(sort);
        rules.save(rule);
    }
}

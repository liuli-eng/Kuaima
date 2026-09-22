-- 已存在 reward_recharge_order 表时执行一次。
ALTER TABLE reward_recharge_order
    ADD COLUMN enterprise_id BIGINT NULL AFTER user_id,
    ADD COLUMN bonus_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 AFTER amount,
    ADD COLUMN pay_method VARCHAR(30) NOT NULL DEFAULT 'WECHAT' AFTER bonus_amount,
    ADD COLUMN expire_at DATETIME NULL AFTER paid_at,
    ADD COLUMN failure_reason VARCHAR(500) NULL AFTER expire_at;

INSERT IGNORE INTO admin_setting(setting_key, setting_value, category, description) VALUES
('reward.recharge.quickAmounts', '0.01:0,1000:30,2000:80,5000:260,10000:600,20000:1400',
 'reward', '奖励金充值快捷金额与赠送金额，格式为本金:赠送金');

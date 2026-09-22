-- 奖励金微信充值订单。应用仍使用 ddl-auto:update 时也建议在发布前显式执行本脚本。
CREATE TABLE IF NOT EXISTS reward_recharge_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(48) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    user_id BIGINT NOT NULL,
    enterprise_id BIGINT NULL,
    amount DECIMAL(18,2) NOT NULL,
    bonus_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    pay_method VARCHAR(30) NOT NULL DEFAULT 'WECHAT',
    status VARCHAR(20) NOT NULL,
    pay_params VARCHAR(4000) NULL,
    reward_credited BOOLEAN NOT NULL DEFAULT FALSE,
    wechat_transaction_id VARCHAR(64) NULL,
    created_at DATETIME NOT NULL,
    paid_at DATETIME NULL,
    expire_at DATETIME NULL,
    failure_reason VARCHAR(500) NULL,
    UNIQUE KEY uk_reward_recharge_order_no (order_no),
    UNIQUE KEY uk_reward_recharge_idempotency (idempotency_key),
    KEY idx_reward_recharge_user_time (user_id, created_at),
    KEY idx_reward_recharge_status_time (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO admin_setting(setting_key, setting_value, category, description) VALUES
('reward.recharge.quickAmounts', '0.01:0,1000:30,2000:80,5000:260,10000:600,20000:1400',
 'reward', '奖励金充值快捷金额与赠送金额，格式为本金:赠送金');

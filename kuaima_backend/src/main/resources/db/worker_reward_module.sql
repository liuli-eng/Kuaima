-- 零工端奖励金接口迁移。
-- 复用 reward_account / reward_flow / reward_withdrawal，保护老板端现有“元”契约；
-- 本组零工端接口出参和入参统一转换为“分”。
-- 本脚本需在应用首次启动前执行一次；后续重复执行前请先确认新增列尚未存在。

ALTER TABLE reward_account
    ADD COLUMN frozen_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 AFTER balance,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER frozen_amount,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER version,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

ALTER TABLE reward_flow
    ADD COLUMN idempotency_key VARCHAR(128) NULL AFTER source_key,
    ADD COLUMN status VARCHAR(20) NULL AFTER idempotency_key,
    ADD COLUMN description VARCHAR(500) NULL AFTER status,
    ADD KEY idx_reward_flow_user_type_time (user_id, type, created_at);

ALTER TABLE reward_withdrawal
    ADD COLUMN channel VARCHAR(20) NOT NULL DEFAULT 'WECHAT' AFTER amount,
    ADD COLUMN merchant_batch_no VARCHAR(64) NULL AFTER idempotency_key,
    ADD COLUMN merchant_detail_no VARCHAR(64) NULL AFTER merchant_batch_no,
    ADD COLUMN wechat_transfer_no VARCHAR(64) NULL AFTER merchant_detail_no,
    ADD COLUMN transfer_response TEXT NULL AFTER wechat_transfer_no,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER failure_reason,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at,
    ADD KEY idx_reward_withdrawal_batch_no (merchant_batch_no),
    ADD KEY idx_reward_withdrawal_detail_no (merchant_detail_no),
    ADD KEY idx_reward_withdrawal_transfer_no (wechat_transfer_no);

CREATE TABLE IF NOT EXISTS reward_earning_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    reward_amount BIGINT NULL COMMENT '固定奖励金额，单位：分',
    action_type VARCHAR(20) NOT NULL,
    action_path VARCHAR(200) NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_reward_earning_rule_code (code),
    KEY idx_reward_earning_rule_enabled_sort (enabled, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO admin_setting(setting_key, setting_value, category, description) VALUES
('reward.withdraw.minimumAmount', '1000', 'reward', '奖励金最低提现金额（分）'),
('reward.withdraw.enabled', 'true', 'reward', '奖励金提现开关'),
('reward.withdraw.disabledReason', '', 'reward', '奖励金提现关闭原因'),
('reward.withdraw.channel', 'WECHAT', 'reward', '奖励金提现渠道');

INSERT IGNORE INTO reward_earning_rule(code, name, description, reward_amount, action_type, action_path, enabled, sort, created_at, updated_at) VALUES
('INVITE_FIRST_ORDER', '邀请好友接单', '好友注册并完成首单后发放', 800, 'NAVIGATE', '/pages/worker/invite', TRUE, 10, NOW(), NOW()),
('INCOME_SHARE', '晒收入分享', '分享收入海报可获得奖励', 200, 'SHARE', NULL, TRUE, 20, NOW(), NOW()),
('ACTIVITY', '活动奖励', '参与平台活动赢取奖励金', NULL, 'NONE', NULL, TRUE, 30, NOW(), NOW());

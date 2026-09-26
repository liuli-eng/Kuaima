-- 测试阶段：奖励金提现只校验金额大于 0 分，不设置 1000 分最低门槛。
-- 该脚本仅更新配置，不会修改账户余额、提现记录或流水。
UPDATE `admin_setting`
SET `setting_value` = '1',
    `description` = '奖励金最低提现金额（分），测试阶段为1分'
WHERE `setting_key` = 'reward.withdraw.minimumAmount';

-- 若目标环境还没有该配置，可先执行：
-- INSERT INTO `admin_setting` (`setting_key`, `setting_value`, `category`, `description`)
-- VALUES ('reward.withdraw.minimumAmount', '1', 'reward', '奖励金最低提现金额（分），测试阶段为1分');

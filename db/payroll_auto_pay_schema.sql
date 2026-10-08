-- =====================================================================
-- 快马日结 - 批量发薪自动扣款付款
-- 场景：发薪单审批通过后，自动从老板商户账户余额扣款（分），
--       逐人打入零工钱包（元），并回填转账记录字段。
--
-- 适用：MySQL 8.x（注意：MySQL **不支持** ADD COLUMN/INDEX IF NOT EXISTS，
--       那是 MariaDB 语法；用 mysql < 脚本 执行时遇到语法错误会中断后续语句，
--       因此这里用 information_schema 判断 + 存储过程实现真正的幂等）。
-- 可重复执行。
-- =====================================================================

DROP PROCEDURE IF EXISTS sp_payroll_auto_pay_schema;

DELIMITER $$
CREATE PROCEDURE sp_payroll_auto_pay_schema()
BEGIN
    -- 1) payroll_detail.user_id：收款人用户 id
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payroll_detail'
                     AND COLUMN_NAME = 'user_id') THEN
        ALTER TABLE `payroll_detail` ADD COLUMN `user_id` BIGINT DEFAULT NULL
            COMMENT '收款人用户 id（project_member.user_id 优先，回退手机号匹配 sys_user）' AFTER `payroll_id`;
    END IF;

    -- 2) payroll_detail.remark：失败原因/备注
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payroll_detail'
                     AND COLUMN_NAME = 'remark') THEN
        ALTER TABLE `payroll_detail` ADD COLUMN `remark` VARCHAR(500) DEFAULT NULL
            COMMENT '备注/失败原因，如 无收款账号、非本项目成员' AFTER `pay_time`;
    END IF;

    -- 3) payroll_order.pay_batch_no：付款批次号（审批幂等兜底）
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payroll_order'
                     AND COLUMN_NAME = 'pay_batch_no') THEN
        ALTER TABLE `payroll_order` ADD COLUMN `pay_batch_no` VARCHAR(64) DEFAULT NULL
            COMMENT '付款批次号，审批通过即生成（幂等键）' AFTER `order_no`;
    END IF;

    -- 4) 索引 idx_pd_user
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payroll_detail'
                     AND INDEX_NAME = 'idx_pd_user') THEN
        ALTER TABLE `payroll_detail` ADD INDEX `idx_pd_user` (`user_id`);
    END IF;

    -- 5) 唯一索引 uk_payroll_pay_batch
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payroll_order'
                     AND INDEX_NAME = 'uk_payroll_pay_batch') THEN
        ALTER TABLE `payroll_order` ADD UNIQUE INDEX `uk_payroll_pay_batch` (`pay_batch_no`);
    END IF;
END$$
DELIMITER ;

CALL sp_payroll_auto_pay_schema();
DROP PROCEDURE IF EXISTS sp_payroll_auto_pay_schema;
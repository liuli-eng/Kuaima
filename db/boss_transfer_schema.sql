-- =====================================================================
-- 快马日结 - 转账记录模块数据库脚本
-- 适用于 MySQL 8.0+（注意：MySQL 不支持 ADD COLUMN/INDEX IF NOT EXISTS，
--   那是 MariaDB 语法；用 mysql < 脚本 执行时遇到语法错误会中断后续语句。
--   因此本脚本用 information_schema 判断 + 存储过程实现真正的幂等，可重复执行）
-- 说明: 转账记录数据来源于已审批通过（approved）的 payroll_order
--       及其 payroll_detail 明细，本脚本补充支付相关字段与示例数据
-- =====================================================================

DROP PROCEDURE IF EXISTS sp_boss_transfer_schema;

DELIMITER $$
CREATE PROCEDURE sp_boss_transfer_schema()
BEGIN
    -- -----------------------------------------------------------------
    -- 变更 1: payroll_order (发薪单) 新增支付相关字段
    -- -----------------------------------------------------------------
    IF EXISTS (SELECT 1 FROM information_schema.TABLES
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payroll_order') THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_order' AND COLUMN_NAME = 'reject_reason') THEN
            ALTER TABLE `payroll_order` ADD COLUMN `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因' AFTER `review_time`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_order' AND COLUMN_NAME = 'pay_account') THEN
            ALTER TABLE `payroll_order` ADD COLUMN `pay_account` VARCHAR(128) DEFAULT NULL COMMENT '支付账户，如 招商银行 · ****6688' AFTER `reject_reason`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_order' AND COLUMN_NAME = 'pay_time') THEN
            ALTER TABLE `payroll_order` ADD COLUMN `pay_time` DATETIME DEFAULT NULL COMMENT '支付日期（批量转账完成时间）' AFTER `pay_account`;
        END IF;
    END IF;

    -- -----------------------------------------------------------------
    -- 变更 2: payroll_detail (发薪明细) 新增转账相关字段
    -- -----------------------------------------------------------------
    IF EXISTS (SELECT 1 FROM information_schema.TABLES
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payroll_detail') THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_detail' AND COLUMN_NAME = 'order_no') THEN
            ALTER TABLE `payroll_detail` ADD COLUMN `order_no` VARCHAR(32) DEFAULT NULL COMMENT '转账单号，如 TR20260910001' AFTER `status`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_detail' AND COLUMN_NAME = 'account') THEN
            ALTER TABLE `payroll_detail` ADD COLUMN `account` VARCHAR(128) DEFAULT NULL COMMENT '支付账户，如 招商银行 · ****6688' AFTER `order_no`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_detail' AND COLUMN_NAME = 'pay_time') THEN
            ALTER TABLE `payroll_detail` ADD COLUMN `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间' AFTER `account`;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_detail' AND INDEX_NAME = 'idx_pd_order_no') THEN
            ALTER TABLE `payroll_detail` ADD INDEX `idx_pd_order_no` (`order_no`);
        END IF;
    END IF;

    -- -----------------------------------------------------------------
    -- 示例数据: payroll_order 转账记录（已审批通过，含支付信息）
    --   按唯一键 order_no 判断，已存在则跳过
    -- -----------------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM `payroll_order` WHERE `order_no` = 'TR20260909001') THEN
        INSERT INTO `payroll_order` (`title`, `project_id`, `project_name`, `type`, `amount`, `people_count`, `creator`, `creator_id`, `submit_time`, `status`, `review_by`, `review_time`, `pay_account`, `pay_time`, `order_no`)
        VALUES ('9月8日费用结算', 1, '菜鸟·云联日结（Gefield）', 'wage', 10000, 1, '黄美玲', 1001, '2026-09-08 20:00:00', 'approved', '老板', '2026-09-09 13:34:00', '招商银行 · ****6688', '2026-09-09 13:34:00', 'TR20260909001');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_order` WHERE `order_no` = 'TR20260909002') THEN
        INSERT INTO `payroll_order` (`title`, `project_id`, `project_name`, `type`, `amount`, `people_count`, `creator`, `creator_id`, `submit_time`, `status`, `review_by`, `review_time`, `pay_account`, `pay_time`, `order_no`)
        VALUES ('9月8日装卸费用结算', 2, '菜鸟·沙溪小时工（运输）', 'wage', 21400, 4, '王小虎', 1001, '2026-09-08 20:10:00', 'approved', '老板', '2026-09-09 13:33:00', '招商银行 · ****6688', '2026-09-09 13:33:00', 'TR20260909002');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_order` WHERE `order_no` = 'TR20260909003') THEN
        INSERT INTO `payroll_order` (`title`, `project_id`, `project_name`, `type`, `amount`, `people_count`, `creator`, `creator_id`, `submit_time`, `status`, `review_by`, `review_time`, `pay_account`, `pay_time`, `order_no`)
        VALUES ('9月8号员工工资', 3, '邮政·茶山小时工（运输）', 'wage', 902300, 51, '黄美玲', 1001, '2026-09-08 20:20:00', 'approved', '老板', '2026-09-09 13:30:00', '工商银行 · ****2211', '2026-09-09 13:30:00', 'TR20260909003');
    END IF;

    -- -----------------------------------------------------------------
    -- 示例数据: payroll_detail 转账明细（含单号/账户/支付时间/状态）
    --   按 (payroll_id, name) 判断，已存在则跳过
    -- -----------------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 8 AND `name` = '陈建国') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (8, '陈建国', '13800138899', '分拣员', 1, 260000, 260000, 'success', 'TR20260910001', '招商银行 · ****6688', '2026-09-10 18:30:00');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 8 AND `name` = '李鸿飞') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (8, '李鸿飞', '13900136688', '分拣员', 1, 320000, 320000, 'success', 'TR20260910001', '招商银行 · ****6688', '2026-09-10 18:30:00');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 8 AND `name` = '周建国') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (8, '周建国', '15000133321', '装车工', 1, 50000, 50000, 'success', 'TR20260911003', '工商银行 · ****2211', '2026-09-11 08:45:00');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 8 AND `name` = '龙晓梅') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (8, '龙晓梅', '18600120912', '装车工', 1, 218000, 218000, 'success', 'TR20260910002', '招商银行 · ****6688', '2026-09-10 18:35:00');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 8 AND `name` = '黄志强') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (8, '黄志强', '13500137743', '装车工', 1, 195000, 195000, 'failed', 'TR20260910002', '招商银行 · ****6688', '2026-09-10 18:35:00');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 8 AND `name` = '李天佑') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (8, '李天佑', '18800135216', '分拣员', 1, 240000, 240000, 'success', 'TR20260909004', '建设银行 · ****7712', '2026-09-09 21:12:00');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 9 AND `name` = '张三') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (9, '张三', '13800138001', '分拣员', 1, 10000, 10000, 'success', 'TR20260909001', '招商银行 · ****6688', '2026-09-09 13:34:00');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 10 AND `name` = '李四') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (10, '李四', '13800138002', '装车工', 4, 5350, 21400, 'success', 'TR20260909002', '招商银行 · ****6688', '2026-09-09 13:33:00');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `payroll_detail` WHERE `payroll_id` = 11 AND `name` = '王五') THEN
        INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`, `order_no`, `account`, `pay_time`)
        VALUES (11, '王五', '13800138003', '分拣员', 51, 17731, 902300, 'success', 'TR20260909003', '工商银行 · ****2211', '2026-09-09 13:30:00');
    END IF;
END$$
DELIMITER ;

CALL sp_boss_transfer_schema();
DROP PROCEDURE IF EXISTS sp_boss_transfer_schema;
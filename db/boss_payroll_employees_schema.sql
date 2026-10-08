-- =====================================================================
-- 快马日结 - 员工列表/员工详情模块数据库脚本
-- 适用于 MySQL 8.0+（注意：MySQL 不支持 ADD COLUMN/INDEX IF NOT EXISTS，
--   那是 MariaDB 语法；用 mysql < 脚本 执行时遇到语法错误会中断后续语句。
--   因此本脚本用 information_schema 判断 + 存储过程实现真正的幂等，可重复执行）
-- 说明: 补充 payroll_employee 员工扩展字段、attendance_record 考勤表
--       及员工详情页所需的考勤示例数据
-- =====================================================================

-- =====================================================================
-- 表: attendance_record (考勤打卡记录)
-- 说明: 员工详情页「最近考勤」数据来源，按姓名匹配
-- =====================================================================
CREATE TABLE IF NOT EXISTS `attendance_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `date` DATE COMMENT '创建日期',
    `create_by` BIGINT COMMENT '创建人ID',
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `project_id` BIGINT DEFAULT NULL COMMENT '关联 project.id',
    `member_id` BIGINT DEFAULT NULL COMMENT '关联 project_member.id',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联用户/员工 id',
    `name` VARCHAR(64) DEFAULT NULL COMMENT '打卡人姓名',
    `attend_date` DATE DEFAULT NULL COMMENT '考勤日期',
    `sign_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
    `sign_out_time` DATETIME DEFAULT NULL COMMENT '签退时间',
    `status` VARCHAR(16) DEFAULT 'on' COMMENT '状态:on出勤/late迟到/absent缺卡/leave请假',
    `late_minutes` INT DEFAULT 0 COMMENT '迟到分钟数',
    PRIMARY KEY (`id`),
    KEY `idx_att_project_date` (`project_id`, `attend_date`),
    KEY `idx_att_member` (`member_id`),
    KEY `idx_att_status` (`status`),
    KEY `idx_att_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤打卡记录';

DROP PROCEDURE IF EXISTS sp_boss_payroll_employees_schema;

DELIMITER $$
CREATE PROCEDURE sp_boss_payroll_employees_schema()
BEGIN
    -- -----------------------------------------------------------------
    -- 变更 1: payroll_employee (发薪员工) 新增员工详情展示字段
    --   表不存在时整体跳过（payroll_employee 由 payroll_approve_schema.sql 建表）
    -- -----------------------------------------------------------------
    IF EXISTS (SELECT 1 FROM information_schema.TABLES
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payroll_employee') THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'gender') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别:男/女' AFTER `name`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'age') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `age` INT DEFAULT NULL COMMENT '年龄' AFTER `gender`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'id_card') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `id_card` VARCHAR(32) DEFAULT NULL COMMENT '身份证号（脱敏存储）' AFTER `phone`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'certified') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `certified` TINYINT(1) DEFAULT 0 COMMENT '是否已实名认证' AFTER `id_card`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'project_id') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `project_id` BIGINT DEFAULT NULL COMMENT '关联 project.id' AFTER `status`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'project_name') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `project_name` VARCHAR(120) DEFAULT NULL COMMENT '所属项目名称' AFTER `project_id`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'position') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `position` VARCHAR(64) DEFAULT NULL COMMENT '岗位' AFTER `project_name`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'employment_type') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `employment_type` VARCHAR(20) DEFAULT NULL COMMENT '用工类型:全职/兼职/临时' AFTER `position`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'daily_wage') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `daily_wage` BIGINT DEFAULT NULL COMMENT '日薪标准（分）' AFTER `employment_type`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'join_date') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `join_date` DATE DEFAULT NULL COMMENT '入职时间' AFTER `daily_wage`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'total_attend_days') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `total_attend_days` INT DEFAULT 0 COMMENT '累计出勤天数' AFTER `add_time`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'total_paid') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `total_paid` BIGINT DEFAULT 0 COMMENT '累计发薪（分）' AFTER `total_attend_days`;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND COLUMN_NAME = 'work_days') THEN
            ALTER TABLE `payroll_employee` ADD COLUMN `work_days` INT DEFAULT 0 COMMENT '在岗天数' AFTER `total_paid`;
        END IF;

        -- 状态字段注释更新（值域: active在职/temp临时/left离职）；MODIFY 本身可重复执行
        ALTER TABLE `payroll_employee` MODIFY COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态:active在职/temp临时/left离职';

        IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'payroll_employee' AND INDEX_NAME = 'idx_pe_status') THEN
            ALTER TABLE `payroll_employee` ADD INDEX `idx_pe_status` (`status`);
        END IF;
    END IF;

    -- -----------------------------------------------------------------
    -- 示例数据: attendance_record (张虎最近考勤)
    --   逐条「不存在才插」，重复执行不会重复写入
    -- -----------------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM `attendance_record` WHERE `project_id` = 2 AND `name` = '张虎' AND `attend_date` = '2026-09-09') THEN
        INSERT INTO `attendance_record` (`project_id`, `name`, `attend_date`, `sign_in_time`, `sign_out_time`, `status`, `late_minutes`)
        VALUES (2, '张虎', '2026-09-09', '2026-09-09 07:55:00', '2026-09-09 17:10:00', 'on', 0);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `attendance_record` WHERE `project_id` = 2 AND `name` = '张虎' AND `attend_date` = '2026-09-08') THEN
        INSERT INTO `attendance_record` (`project_id`, `name`, `attend_date`, `sign_in_time`, `sign_out_time`, `status`, `late_minutes`)
        VALUES (2, '张虎', '2026-09-08', '2026-09-08 08:15:00', '2026-09-08 17:00:00', 'late', 15);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `attendance_record` WHERE `project_id` = 2 AND `name` = '张虎' AND `attend_date` = '2026-09-07') THEN
        INSERT INTO `attendance_record` (`project_id`, `name`, `attend_date`, `sign_in_time`, `sign_out_time`, `status`, `late_minutes`)
        VALUES (2, '张虎', '2026-09-07', NULL, NULL, 'leave', 0);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `attendance_record` WHERE `project_id` = 2 AND `name` = '张虎' AND `attend_date` = '2026-09-06') THEN
        INSERT INTO `attendance_record` (`project_id`, `name`, `attend_date`, `sign_in_time`, `sign_out_time`, `status`, `late_minutes`)
        VALUES (2, '张虎', '2026-09-06', '2026-09-06 07:50:00', '2026-09-06 17:05:00', 'on', 0);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `attendance_record` WHERE `project_id` = 2 AND `name` = '张虎' AND `attend_date` = '2026-09-05') THEN
        INSERT INTO `attendance_record` (`project_id`, `name`, `attend_date`, `sign_in_time`, `sign_out_time`, `status`, `late_minutes`)
        VALUES (2, '张虎', '2026-09-05', '2026-09-05 07:58:00', '2026-09-05 17:02:00', 'on', 0);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `attendance_record` WHERE `project_id` = 1 AND `name` = '王铁柱' AND `attend_date` = '2026-09-09') THEN
        INSERT INTO `attendance_record` (`project_id`, `name`, `attend_date`, `sign_in_time`, `sign_out_time`, `status`, `late_minutes`)
        VALUES (1, '王铁柱', '2026-09-09', '2026-09-09 08:00:00', '2026-09-09 17:30:00', 'on', 0);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM `attendance_record` WHERE `project_id` = 3 AND `name` = '李长安' AND `attend_date` = '2026-09-09') THEN
        INSERT INTO `attendance_record` (`project_id`, `name`, `attend_date`, `sign_in_time`, `sign_out_time`, `status`, `late_minutes`)
        VALUES (3, '李长安', '2026-09-09', '2026-09-09 07:45:00', '2026-09-09 17:00:00', 'on', 0);
    END IF;
END$$
DELIMITER ;

CALL sp_boss_payroll_employees_schema();
DROP PROCEDURE IF EXISTS sp_boss_payroll_employees_schema;
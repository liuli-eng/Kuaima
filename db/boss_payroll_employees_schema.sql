-- =====================================================================
-- 快马日结 - 员工列表/员工详情模块数据库脚本
-- 适用于 MySQL 8.0+
-- 说明: 补充 payroll_employee 员工扩展字段、attendance_record 考勤表
--       及员工详情页所需的考勤示例数据
-- =====================================================================

-- =====================================================================
-- 变更 1: payroll_employee (发薪员工) 新增员工详情展示字段
-- =====================================================================
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别:男/女' AFTER `name`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `age` INT DEFAULT NULL COMMENT '年龄' AFTER `gender`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `id_card` VARCHAR(32) DEFAULT NULL COMMENT '身份证号（脱敏存储）' AFTER `phone`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `certified` TINYINT(1) DEFAULT 0 COMMENT '是否已实名认证' AFTER `id_card`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `project_id` BIGINT DEFAULT NULL COMMENT '关联 project.id' AFTER `status`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `project_name` VARCHAR(120) DEFAULT NULL COMMENT '所属项目名称' AFTER `project_id`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `position` VARCHAR(64) DEFAULT NULL COMMENT '岗位' AFTER `project_name`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `employment_type` VARCHAR(20) DEFAULT NULL COMMENT '用工类型:全职/兼职/临时' AFTER `position`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `daily_wage` BIGINT DEFAULT NULL COMMENT '日薪标准（分）' AFTER `employment_type`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `join_date` DATE DEFAULT NULL COMMENT '入职时间' AFTER `daily_wage`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `total_attend_days` INT DEFAULT 0 COMMENT '累计出勤天数' AFTER `add_time`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `total_paid` BIGINT DEFAULT 0 COMMENT '累计发薪（分）' AFTER `total_attend_days`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `work_days` INT DEFAULT 0 COMMENT '在岗天数' AFTER `total_paid`;

-- 状态字段注释更新（值域: active在职/temp临时/left离职）
ALTER TABLE `payroll_employee` MODIFY COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态:active在职/temp临时/left离职';
ALTER TABLE `payroll_employee` ADD INDEX IF NOT EXISTS `idx_pe_status` (`status`);

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

-- =====================================================================
-- 示例数据: attendance_record (张虎最近考勤)
-- =====================================================================
INSERT INTO `attendance_record` (`project_id`, `name`, `attend_date`, `sign_in_time`, `sign_out_time`, `status`, `late_minutes`)
VALUES
(2, '张虎', '2026-09-09', '2026-09-09 07:55:00', '2026-09-09 17:10:00', 'on', 0),
(2, '张虎', '2026-09-08', '2026-09-08 08:15:00', '2026-09-08 17:00:00', 'late', 15),
(2, '张虎', '2026-09-07', NULL, NULL, 'leave', 0),
(2, '张虎', '2026-09-06', '2026-09-06 07:50:00', '2026-09-06 17:05:00', 'on', 0),
(2, '张虎', '2026-09-05', '2026-09-05 07:58:00', '2026-09-05 17:02:00', 'on', 0),
(1, '王铁柱', '2026-09-09', '2026-09-09 08:00:00', '2026-09-09 17:30:00', 'on', 0),
(3, '李长安', '2026-09-09', '2026-09-09 07:45:00', '2026-09-09 17:00:00', 'on', 0);

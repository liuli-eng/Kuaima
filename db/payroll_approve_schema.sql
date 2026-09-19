-- =====================================================================
-- 快马日结 - 审批管理模块数据库脚本
-- 适用于 MySQL 8.0+
-- =====================================================================

-- =====================================================================
-- 表: payroll_order (发薪单)
-- 说明: 存储老板创建的发薪单信息，包含审批状态
-- =====================================================================
CREATE TABLE IF NOT EXISTS `payroll_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `date` DATE COMMENT '创建日期',
    `create_by` BIGINT COMMENT '创建人ID',
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `order_no` VARCHAR(32) DEFAULT NULL COMMENT '发薪单号，如 TR20260910001',
    `company` VARCHAR(128) DEFAULT NULL COMMENT '所属公司',
    `title` VARCHAR(255) NOT NULL COMMENT '薪单标题',
    `project_id` BIGINT DEFAULT NULL COMMENT '关联 project.id',
    `project_name` VARCHAR(128) DEFAULT NULL COMMENT '所属项目',
    `type` VARCHAR(16) DEFAULT 'wage' COMMENT '转账类型:wage工资/advance预支/other其他',
    `amount` BIGINT DEFAULT 0 COMMENT '应发总金额（分）',
    `people_count` INT DEFAULT 0 COMMENT '发薪人数',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '制单人员',
    `creator_id` BIGINT DEFAULT NULL COMMENT '制单人员ID',
    `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
    `status` VARCHAR(16) DEFAULT 'pending' COMMENT '状态:pending待审批/approved审批通过/rejected已驳回/withdrawn已撤回',
    `review_by` VARCHAR(64) DEFAULT NULL COMMENT '审核人员',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    `pay_account` VARCHAR(128) DEFAULT NULL COMMENT '支付账户，如 招商银行 · ****6688',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付日期（批量转账完成时间）',
    PRIMARY KEY (`id`),
    KEY `idx_payroll_project` (`project_id`),
    KEY `idx_payroll_status` (`status`),
    KEY `idx_payroll_submit` (`submit_time`),
    KEY `idx_payroll_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发薪单（后台发薪管理核心实体）';

-- =====================================================================
-- 表: payroll_detail (发薪明细)
-- 说明: 发薪单下的人员明细信息
-- =====================================================================
CREATE TABLE IF NOT EXISTS `payroll_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `date` DATE COMMENT '创建日期',
    `create_by` BIGINT COMMENT '创建人ID',
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `payroll_id` BIGINT NOT NULL COMMENT '关联 payroll_order.id',
    `name` VARCHAR(64) DEFAULT NULL COMMENT '姓名',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    `job` VARCHAR(64) DEFAULT NULL COMMENT '岗位',
    `attend_days` INT DEFAULT 0 COMMENT '出勤天数',
    `daily_wage` BIGINT DEFAULT 0 COMMENT '日薪（分）',
    `amount` BIGINT DEFAULT 0 COMMENT '应发金额（分）',
    `status` VARCHAR(16) DEFAULT 'pending' COMMENT '状态:pending待转账/success转账成功/failed转账失败',
    `order_no` VARCHAR(32) DEFAULT NULL COMMENT '转账单号，如 TR20260910001',
    `account` VARCHAR(128) DEFAULT NULL COMMENT '支付账户，如 招商银行 · ****6688',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    PRIMARY KEY (`id`),
    KEY `idx_pd_payroll` (`payroll_id`),
    KEY `idx_pd_status` (`status`),
    KEY `idx_pd_phone` (`phone`),
    KEY `idx_pd_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发薪明细（发薪单下的人员明细）';

-- =====================================================================
-- 表: payroll_employee (发薪员工)
-- 说明: 老板的发薪员工库
-- =====================================================================
CREATE TABLE IF NOT EXISTS `payroll_employee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `date` DATE COMMENT '创建日期',
    `create_by` BIGINT COMMENT '创建人ID',
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `boss_id` BIGINT NOT NULL COMMENT '老板ID',
    `name` VARCHAR(64) DEFAULT NULL COMMENT '姓名',
    `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别:男/女',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    `id_card` VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
    `certified` TINYINT(1) DEFAULT 0 COMMENT '是否实名认证',
    `status` VARCHAR(16) DEFAULT 'active' COMMENT '状态:active在职/temp临时/left离职',
    `project_id` BIGINT DEFAULT NULL COMMENT '关联 project.id',
    `project_name` VARCHAR(120) DEFAULT NULL COMMENT '所属项目名称',
    `position` VARCHAR(64) DEFAULT NULL COMMENT '岗位',
    `employment_type` VARCHAR(20) DEFAULT NULL COMMENT '用工类型:全职/兼职/临时',
    `daily_wage` BIGINT DEFAULT NULL COMMENT '日薪标准（分）',
    `join_date` DATE DEFAULT NULL COMMENT '入职时间',
    `add_time` DATETIME DEFAULT NULL COMMENT '添加时间',
    `total_attend_days` INT DEFAULT 0 COMMENT '累计出勤天数',
    `total_paid` BIGINT DEFAULT 0 COMMENT '累计发薪金额（分）',
    `work_days` INT DEFAULT 0 COMMENT '在岗天数',
    PRIMARY KEY (`id`),
    KEY `idx_pe_boss` (`boss_id`),
    KEY `idx_pe_status` (`status`),
    KEY `idx_pe_phone` (`phone`),
    KEY `idx_pe_addtime` (`add_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发薪员工（老板的发薪员工库）';

-- =====================================================================
-- 现有表结构变更（如已存在 payroll_order / payroll_employee 表，则执行以下 ALTER）
-- =====================================================================

-- payroll_order 新增字段
ALTER TABLE `payroll_order` ADD COLUMN IF NOT EXISTS `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因' AFTER `review_time`;
ALTER TABLE `payroll_order` ADD COLUMN IF NOT EXISTS `pay_account` VARCHAR(128) DEFAULT NULL COMMENT '支付账户' AFTER `reject_reason`;
ALTER TABLE `payroll_order` ADD COLUMN IF NOT EXISTS `pay_time` DATETIME DEFAULT NULL COMMENT '支付日期' AFTER `pay_account`;

-- payroll_employee 新增字段（兼容旧表结构升级）
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别' AFTER `name`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `age` INT DEFAULT NULL COMMENT '年龄' AFTER `gender`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `id_card` VARCHAR(32) DEFAULT NULL COMMENT '身份证号' AFTER `phone`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `project_id` BIGINT DEFAULT NULL COMMENT '关联 project.id' AFTER `status`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `project_name` VARCHAR(120) DEFAULT NULL COMMENT '所属项目名称' AFTER `project_id`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `position` VARCHAR(64) DEFAULT NULL COMMENT '岗位' AFTER `project_name`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `employment_type` VARCHAR(20) DEFAULT NULL COMMENT '用工类型' AFTER `position`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `daily_wage` BIGINT DEFAULT NULL COMMENT '日薪标准（分）' AFTER `employment_type`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `join_date` DATE DEFAULT NULL COMMENT '入职时间' AFTER `daily_wage`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `certified` TINYINT(1) DEFAULT 0 COMMENT '是否实名认证' AFTER `id_card`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `total_attend_days` INT DEFAULT 0 COMMENT '累计出勤天数' AFTER `add_time`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `total_paid` BIGINT DEFAULT 0 COMMENT '累计发薪金额（分）' AFTER `total_attend_days`;
ALTER TABLE `payroll_employee` ADD COLUMN IF NOT EXISTS `work_days` INT DEFAULT 0 COMMENT '在岗天数' AFTER `total_paid`;

-- =====================================================================
-- 示例数据: payroll_order (发薪单示例)
-- =====================================================================
-- 待审批示例数据
INSERT INTO `payroll_order` (`title`, `project_id`, `project_name`, `type`, `amount`, `people_count`, `creator`, `creator_id`, `submit_time`, `status`)
VALUES
('2026-09-10 分拣日结发薪', 1, '菜鸟·云联日结（Gefield）', 'wage', 3260000, 26, '陈建国', 1001, '2026-09-10 18:22:00', 'pending'),
('2026-09-10 装车日结发薪', 2, '菜鸟·沙溪日结（真实）', 'wage', 5423000, 51, '陈美玲', 1001, '2026-09-10 17:45:00', 'pending'),
('周建国 预支申请', 2, '菜鸟·沙溪日结（真实）', 'advance', 50000, 1, '周建国', 1001, '2026-09-11 08:30:00', 'pending'),
('2026-09-09 茶叶分拣日结', 3, '邮政·茶山日结（真实）', 'wage', 4108800, 32, '李鸿飞', 1001, '2026-09-09 21:05:00', 'pending');

-- 已通过示例数据
INSERT INTO `payroll_order` (`title`, `project_id`, `project_name`, `type`, `amount`, `people_count`, `creator`, `creator_id`, `submit_time`, `status`, `review_by`, `review_time`)
VALUES
('2月24日分拣分拣', 1, '菜鸟·云联日结（Gefield）', 'wage', 168450, 1, '孙晓清', 1001, '2026-02-24 10:00:00', 'approved', '老板', '2026-02-24 20:19:00'),
('2月24日装车', 2, '菜鸟·沙溪日结（真实）', 'wage', 134300, 1, '孙晓清', 1001, '2026-02-24 09:30:00', 'approved', '老板', '2026-02-24 17:17:00'),
('23号茶苑日结', 3, '邮政·茶山日结（真实）', 'wage', 95400, 1, '用户0168', 1001, '2026-02-23 14:00:00', 'approved', '老板', '2026-02-24 17:16:00');

-- =====================================================================
-- 示例数据: payroll_detail (发薪明细示例)
-- =====================================================================
INSERT INTO `payroll_detail` (`payroll_id`, `name`, `phone`, `job`, `attend_days`, `daily_wage`, `amount`, `status`)
VALUES
(5, '张三', '13800138001', '分拣员', 1, 168450, 168450, 'success'),
(6, '李四', '13800138002', '装车工', 1, 134300, 134300, 'success'),
(7, '王五', '13800138003', '分拣员', 1, 95400, 95400, 'success');

-- =====================================================================
-- 示例数据: payroll_employee (发薪员工示例，含完整字段)
-- =====================================================================
INSERT INTO `payroll_employee` (`boss_id`, `name`, `gender`, `age`, `phone`, `id_card`, `certified`, `status`, `project_id`, `project_name`, `position`, `employment_type`, `daily_wage`, `join_date`, `add_time`, `total_attend_days`, `total_paid`, `work_days`)
VALUES
(1001, '张虎', '男', 40, '13642285678', '360123198608151234', 1, 'active', 2, '菜鸟·沙溪日结（真实）', '分拣打包员', '全职', 18000, '2026-08-20', '2026-08-20 18:17:00', 26, 586000, 12),
(1001, '王铁柱', '男', 32, '41112223333', '410102199405125678', 1, 'active', 1, '菜鸟·云联日结（Gefield）', '装车工', '全职', 16000, '2026-08-19', '2026-08-19 18:26:00', 18, 432000, 8),
(1001, '李长安', '男', 30, '52362816901', '520102199603072345', 1, 'active', 3, '邮政·茶山日结（真实）', '分拣员', '全职', 17000, '2026-08-19', '2026-08-19 19:53:00', 15, 368000, 7),
(1001, '刘芝麻', '女', 36, '15822330019', '152523199008204567', 1, 'temp', 2, '菜鸟·沙溪日结（真实）', '分拣员', '临时', 15000, '2026-09-02', '2026-09-02 10:05:00', 12, 216000, 5),
(1001, '赵有才', '男', 45, '13900128866', '130104198109153456', 1, 'left', NULL, '已离职', '装车工', '全职', 18000, '2025-06-15', '2026-09-02 14:40:00', 48, 892000, 20);

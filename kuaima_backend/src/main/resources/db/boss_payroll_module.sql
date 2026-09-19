-- =============================================================
-- 老板端批量发薪模块（uniapp：发薪 / 员工 / 员工详情 / 已审批记录）
-- 适用：MySQL 8.x / InnoDB / utf8mb4
-- 说明：发薪单复用已有 payroll_order / payroll_detail 表；
--       本脚本新增老板端发薪员工表。金额统一以「分」为单位存储。
-- =============================================================

CREATE TABLE IF NOT EXISTS `payroll_employee` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `boss_id`          BIGINT       NOT NULL COMMENT '老板用户 id',
  `name`             VARCHAR(50)  NOT NULL COMMENT '姓名',
  `gender`           VARCHAR(10)  DEFAULT NULL COMMENT '性别:男/女',
  `age`              INT          DEFAULT NULL COMMENT '年龄',
  `phone`            VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `id_card`          VARCHAR(30)  DEFAULT NULL COMMENT '身份证号（脱敏存储）',
  `certified`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已实名认证',
  `status`           VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态:active在职/temp临时/left离职',
  `project_id`       BIGINT       DEFAULT NULL COMMENT '关联 project.id',
  `project_name`     VARCHAR(120) DEFAULT NULL COMMENT '所属项目名称',
  `position`         VARCHAR(50)  DEFAULT NULL COMMENT '岗位，如 分拣打包员',
  `employment_type`  VARCHAR(20)  DEFAULT NULL COMMENT '用工类型:全职/兼职/临时',
  `daily_wage`       BIGINT       DEFAULT NULL COMMENT '日薪标准（分），如 180元/天=18000',
  `join_date`        DATE         DEFAULT NULL COMMENT '入职时间',
  `add_time`         DATETIME     DEFAULT NULL COMMENT '添加时间',
  `total_attend_days` INT         NOT NULL DEFAULT 0 COMMENT '累计出勤天数',
  `total_paid`       BIGINT       NOT NULL DEFAULT 0 COMMENT '累计发薪（分）',
  `work_days`        INT          NOT NULL DEFAULT 0 COMMENT '在岗天数',
  `create_by`        BIGINT       DEFAULT NULL COMMENT '创建人',
  `date`             DATE         DEFAULT NULL COMMENT '创建日期',
  `timestamp`        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pe_boss` (`boss_id`),
  KEY `idx_pe_status` (`status`),
  KEY `idx_pe_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老板端发薪员工';

-- 初始化示例数据（boss_id=1 演示数据）
INSERT INTO `payroll_employee` (`boss_id`, `name`, `gender`, `age`, `phone`, `id_card`, `certified`, `status`, `project_name`, `position`, `employment_type`, `daily_wage`, `join_date`, `add_time`, `total_attend_days`, `total_paid`, `work_days`, `date`) VALUES
(1, '张虎', '男', 40, '13642285678', '3601****19860815', 1, 'active', '菜鸟·沙溪日结（真实）', '分拣打包员', '全职', 18000, '2026-08-20', '2026-08-20 18:17:00', 26, 586000, 12, CURDATE()),
(1, '王铁柱', '男', 32, '41112223333', '4101****19940322', 1, 'active', '菜鸟·云联日结（Gefield）', '装卸工', '全职', 20000, '2026-08-19', '2026-08-19 18:26:00', 18, 432000, 8, CURDATE()),
(1, '李长安', '男', 30, '52362816901', '5201****19960711', 1, 'active', '邮政·茶山日结（真实）', '扫描员', '全职', 16000, '2026-08-19', '2026-08-19 19:53:00', 15, 368000, 7, CURDATE()),
(1, '刘芝麻', '女', 36, '15822330019', '3601****19900214', 0, 'temp', '菜鸟·沙溪日结（真实）', '理货员', '临时', 15000, '2026-09-02', '2026-09-02 10:05:00', 12, 216000, 5, CURDATE()),
(1, '赵有才', '男', 45, '13900128866', '3601****19810506', 1, 'left', '已离职', '分拣打包员', '全职', 18000, '2026-03-01', '2026-09-02 14:40:00', 48, 892000, 20, CURDATE())
ON DUPLICATE KEY UPDATE `timestamp` = CURRENT_TIMESTAMP;

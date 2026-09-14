-- =============================================================
-- 发薪管理模块（后台 admin-web）
-- 适用：MySQL 8.x / InnoDB / utf8mb4
-- 说明：发薪单（薪单）与发薪明细。金额统一以「分」存储（BIGINT）。
-- 发薪流程：①开批发薪 → ②添加人员 → ③设置薪资 → ④提交支付，
-- 提交后需管理员审批通过才会打款。
-- 可重复执行（IF NOT EXISTS）。
-- =============================================================

CREATE TABLE IF NOT EXISTS `payroll_order` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`     VARCHAR(30)  DEFAULT NULL COMMENT '发薪单号，如 TR20260910001',
  `company`      VARCHAR(120) DEFAULT NULL COMMENT '所属公司',
  `title`        VARCHAR(100) NOT NULL COMMENT '薪单标题',
  `project_id`   BIGINT       DEFAULT NULL COMMENT '关联 project.id',
  `project_name` VARCHAR(100) DEFAULT NULL COMMENT '所属项目',
  `type`         VARCHAR(20)  NOT NULL DEFAULT 'wage' COMMENT '转账类型:wage工资/advance预支/other其他',
  `amount`       BIGINT       NOT NULL DEFAULT 0 COMMENT '应发总金额（分）',
  `people_count` INT          NOT NULL DEFAULT 0 COMMENT '发薪人数',
  `creator`      VARCHAR(50)  DEFAULT NULL COMMENT '制单人员',
  `creator_id`   BIGINT       DEFAULT NULL COMMENT '制单人员 id',
  `submit_time`  DATETIME     DEFAULT NULL COMMENT '提交时间',
  `status`       VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态:pending待审批/approved审批通过/rejected已驳回/withdrawn已撤回',
  `review_by`    VARCHAR(50)  DEFAULT NULL COMMENT '审核人员',
  `review_time`  DATETIME     DEFAULT NULL COMMENT '审核时间',
  `create_by`    BIGINT       DEFAULT NULL COMMENT '创建人',
  `date`         DATE         DEFAULT NULL COMMENT '创建日期',
  `timestamp`    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payroll_order_no` (`order_no`),
  KEY `idx_payroll_project` (`project_id`),
  KEY `idx_payroll_status` (`status`),
  KEY `idx_payroll_submit` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发薪单';

CREATE TABLE IF NOT EXISTS `payroll_detail` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `payroll_id`  BIGINT      NOT NULL COMMENT '关联 payroll_order.id',
  `name`        VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone`       VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `job`         VARCHAR(50) DEFAULT NULL COMMENT '岗位',
  `attend_days` INT         DEFAULT 0 COMMENT '出勤天数',
  `daily_wage`  BIGINT      DEFAULT 0 COMMENT '日薪（分）',
  `amount`      BIGINT      NOT NULL DEFAULT 0 COMMENT '应发金额（分）',
  `status`      VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态:pending待转账/success转账成功/failed转账失败',
  `create_by`   BIGINT      DEFAULT NULL COMMENT '创建人',
  `date`        DATE        DEFAULT NULL COMMENT '创建日期',
  `timestamp`   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pd_payroll` (`payroll_id`),
  KEY `idx_pd_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发薪明细';

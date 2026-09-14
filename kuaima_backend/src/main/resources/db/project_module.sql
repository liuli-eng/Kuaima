-- =============================================================
-- 项目管理模块（老板端 uniapp）
-- 适用：MySQL 8.x / InnoDB / utf8mb4
-- 说明：表结构可由 JPA ddl-auto=update 自动建表；本脚本用于
--       初始化、CI 环境与手动补表，可重复执行（IF NOT EXISTS）。
-- 金额统一以「分」为单位存储（BIGINT），避免浮点误差。
-- =============================================================

CREATE TABLE IF NOT EXISTS `project` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`              VARCHAR(100) NOT NULL COMMENT '项目名称',
  `company_name`      VARCHAR(120) DEFAULT NULL COMMENT '用工企业',
  `leader_name`       VARCHAR(50)  DEFAULT NULL COMMENT '负责人姓名',
  `leader_phone`      VARCHAR(20)  DEFAULT NULL COMMENT '负责人电话',
  `establish_date`    DATE          DEFAULT NULL COMMENT '立项日期',
  `location`          VARCHAR(200) DEFAULT NULL COMMENT '项目地点',
  `status`            VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态:active进行中/archived已归档/deleted已删除',
  `payroll_cycle`     VARCHAR(30)  DEFAULT NULL COMMENT '发薪周期，如 月结',
  `settle_type`       VARCHAR(30)  DEFAULT NULL COMMENT '结算方式，如 按天结算',
  `daily_wage`        BIGINT       DEFAULT NULL COMMENT '结算单价（分），如 180元/天=18000',
  `location_checkin`  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否开启定位打卡',
  `checkin_radius`    INT          NOT NULL DEFAULT 200 COMMENT '打卡有效范围（米）',
  `sign_code_expire`  INT          NOT NULL DEFAULT 60 COMMENT '签到码有效期（分钟）',
  `late_auto`         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否迟到自动判定',
  `late_threshold`    INT          NOT NULL DEFAULT 10 COMMENT '迟到阈值（分钟）',
  `salary_remind`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '发薪提醒开关',
  `create_by`         BIGINT       DEFAULT NULL COMMENT '创建人',
  `date`              DATE         DEFAULT NULL COMMENT '创建日期',
  `timestamp`         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_status` (`status`),
  KEY `idx_project_company` (`company_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目';

CREATE TABLE IF NOT EXISTS `project_member` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id`  BIGINT      NOT NULL COMMENT '关联 project.id',
  `user_id`     BIGINT      DEFAULT NULL COMMENT '关联用户/员工 id（外部零工可空）',
  `name`        VARCHAR(50) NOT NULL COMMENT '成员姓名',
  `phone`       VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `role`        VARCHAR(50) DEFAULT NULL COMMENT '职位/岗位，如 分拣员、装车员',
  `status`      VARCHAR(20) NOT NULL DEFAULT 'temp' COMMENT '状态:active在职/temp临时/left已离职',
  `join_date`   DATE        DEFAULT NULL COMMENT '入职日期',
  `create_by`   BIGINT      DEFAULT NULL COMMENT '创建人',
  `date`        DATE        DEFAULT NULL COMMENT '创建日期',
  `timestamp`   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pm_project` (`project_id`),
  KEY `idx_pm_status` (`status`),
  KEY `idx_pm_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员';

CREATE TABLE IF NOT EXISTS `project_onsite_staff` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id`  BIGINT      NOT NULL COMMENT '关联 project.id',
  `user_id`     BIGINT      DEFAULT NULL COMMENT '关联员工 id',
  `name`        VARCHAR(50) NOT NULL COMMENT '驻场人员姓名',
  `phone`       VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `company`     VARCHAR(120) DEFAULT NULL COMMENT '所属公司',
  `onsite_role` VARCHAR(20) NOT NULL DEFAULT 'assistant' COMMENT '驻场角色:leader项目负责人/assistant项目助理',
  `onsite_days` INT         NOT NULL DEFAULT 0 COMMENT '驻场天数',
  `create_by`   BIGINT      DEFAULT NULL COMMENT '创建人',
  `date`        DATE        DEFAULT NULL COMMENT '创建日期',
  `timestamp`   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pos_project` (`project_id`),
  KEY `idx_pos_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驻场人员';

CREATE TABLE IF NOT EXISTS `attendance_record` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id`     BIGINT      NOT NULL COMMENT '关联 project.id',
  `member_id`      BIGINT      DEFAULT NULL COMMENT '关联 project_member.id',
  `user_id`        BIGINT      DEFAULT NULL COMMENT '关联用户/员工 id',
  `name`           VARCHAR(50) DEFAULT NULL COMMENT '打卡人姓名',
  `attend_date`    DATE        NOT NULL COMMENT '考勤日期',
  `sign_in_time`   DATETIME    DEFAULT NULL COMMENT '签到时间',
  `sign_out_time`  DATETIME    DEFAULT NULL COMMENT '签退时间',
  `status`         VARCHAR(20) NOT NULL DEFAULT 'on' COMMENT '状态:on出勤/late迟到/absent缺卡/leave请假',
  `late_minutes`   INT         DEFAULT 0 COMMENT '迟到分钟数',
  `create_by`      BIGINT      DEFAULT NULL COMMENT '创建人',
  `date`           DATE        DEFAULT NULL COMMENT '创建日期',
  `timestamp`      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_att_project_date` (`project_id`, `attend_date`),
  KEY `idx_att_member` (`member_id`),
  KEY `idx_att_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤打卡记录';

CREATE TABLE IF NOT EXISTS `onboard_apply` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_id`    BIGINT      NOT NULL COMMENT '关联 project.id',
  `name`          VARCHAR(50) NOT NULL COMMENT '申请人姓名',
  `phone`         VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `job`           VARCHAR(50) DEFAULT NULL COMMENT '申请职位',
  `apply_role`    VARCHAR(30) DEFAULT NULL COMMENT '申请角色，如 项目负责人',
  `intent_project` VARCHAR(100) DEFAULT NULL COMMENT '意向项目',
  `apply_time`    DATETIME    DEFAULT NULL COMMENT '申请时间',
  `status`        VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态:pending审核中/passed已通过/rejected已拒绝',
  `join_date`     DATE        DEFAULT NULL COMMENT '入职日期',
  `salary_expect` VARCHAR(30) DEFAULT NULL COMMENT '期望薪资',
  `work_time`     VARCHAR(20) DEFAULT NULL COMMENT '工时类型，如 全职/兼职',
  `company`       VARCHAR(120) DEFAULT NULL COMMENT '所属公司',
  `reviewer`      VARCHAR(50) DEFAULT NULL COMMENT '审核人',
  `review_time`   DATETIME    DEFAULT NULL COMMENT '审核时间',
  `create_by`     BIGINT      DEFAULT NULL COMMENT '创建人',
  `date`          DATE        DEFAULT NULL COMMENT '创建日期',
  `timestamp`     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ob_project` (`project_id`),
  KEY `idx_ob_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入职申请记录';

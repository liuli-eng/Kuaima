-- =============================================================
-- 员工管理模块（后台 admin-web）
-- 适用：MySQL 8.x / InnoDB / utf8mb4
-- 说明：员工（企业内外部成员）、角色权限、员工参与项目、加入申请。
-- 可重复执行（IF NOT EXISTS）。
-- =============================================================

CREATE TABLE IF NOT EXISTS `employee` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`          VARCHAR(50)  NOT NULL COMMENT '姓名',
  `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `job`           VARCHAR(50)  DEFAULT NULL COMMENT '职位，如 总经理、人事主管',
  `company`       VARCHAR(120) DEFAULT NULL COMMENT '所属公司',
  `role`          VARCHAR(20)  NOT NULL DEFAULT 'staff' COMMENT '角色编码:super超级管理员/admin管理员/staff员工',
  `role_name`     VARCHAR(30)  DEFAULT NULL COMMENT '角色名称，如 超级管理员',
  `role_id`       BIGINT       DEFAULT NULL COMMENT '关联 employee_role.id',
  `status`        VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态:active在职/frozen已停用',
  `permissions`   TEXT         DEFAULT NULL COMMENT '权限树 JSON',
  `avatar_color`  VARCHAR(20)  DEFAULT NULL COMMENT '头像底色标识',
  `add_time`      DATETIME     DEFAULT NULL COMMENT '添加时间',
  `create_by`     BIGINT       DEFAULT NULL COMMENT '创建人',
  `date`          DATE         DEFAULT NULL COMMENT '创建日期',
  `timestamp`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_emp_company` (`company`),
  KEY `idx_emp_role` (`role`),
  KEY `idx_emp_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业员工';

CREATE TABLE IF NOT EXISTS `employee_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(50)  NOT NULL COMMENT '角色名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `system`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否系统内置角色（不可删）',
  `color`       VARCHAR(20)  DEFAULT NULL COMMENT '角色标识色',
  `icon`        VARCHAR(30)  DEFAULT NULL COMMENT '角色图标',
  `permissions` TEXT         DEFAULT NULL COMMENT '权限 JSON',
  `create_by`   BIGINT       DEFAULT NULL COMMENT '创建人',
  `date`        DATE         DEFAULT NULL COMMENT '创建日期',
  `timestamp`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工角色';

CREATE TABLE IF NOT EXISTS `employee_project` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `employee_id`     BIGINT      NOT NULL COMMENT '关联 employee.id',
  `project_id`      BIGINT      NOT NULL COMMENT '关联 project.id',
  `role_in_project` VARCHAR(50) DEFAULT NULL COMMENT '项目内角色，如 项目负责人',
  `joined_date`     DATE        DEFAULT NULL COMMENT '加入项目日期',
  `status`          VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态:active进行中/left已退出',
  `create_by`       BIGINT      DEFAULT NULL COMMENT '创建人',
  `date`            DATE        DEFAULT NULL COMMENT '创建日期',
  `timestamp`       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_proj` (`employee_id`, `project_id`),
  KEY `idx_ep_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工参与项目';

CREATE TABLE IF NOT EXISTS `join_apply` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`          VARCHAR(50) NOT NULL COMMENT '申请人姓名',
  `phone`         VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `job`           VARCHAR(50) DEFAULT NULL COMMENT '申请职位',
  `apply_role`    VARCHAR(30) DEFAULT NULL COMMENT '申请角色',
  `intent_project` VARCHAR(100) DEFAULT NULL COMMENT '意向项目',
  `apply_time`    DATETIME    DEFAULT NULL COMMENT '申请时间',
  `status`        VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态:pending待审批/approved已通过/rejected已拒绝',
  `salary_expect` VARCHAR(30) DEFAULT NULL COMMENT '期望薪资',
  `work_time`     VARCHAR(20) DEFAULT NULL COMMENT '工时类型，如 全职/兼职',
  `join_date`     DATE        DEFAULT NULL COMMENT '入职时间',
  `company`       VARCHAR(120) DEFAULT NULL COMMENT '所属公司',
  `reviewer`      VARCHAR(50) DEFAULT NULL COMMENT '审核人',
  `review_time`   DATETIME    DEFAULT NULL COMMENT '审核时间',
  `create_by`     BIGINT      DEFAULT NULL COMMENT '创建人',
  `date`          DATE        DEFAULT NULL COMMENT '创建日期',
  `timestamp`     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ja_status` (`status`),
  KEY `idx_ja_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加入申请记录';

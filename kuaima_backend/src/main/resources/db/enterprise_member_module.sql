-- 企业成员模块：成员扩展字段、入企申请、邀请记录
-- 重复执行安全（IF NOT EXISTS / 判断后执行）

-- 1. enterprise_member 扩展字段：职位描述、邀请人
ALTER TABLE `enterprise_member` ADD COLUMN IF NOT EXISTS `title` VARCHAR(50) DEFAULT NULL COMMENT '职位描述（如：项目负责人）';
ALTER TABLE `enterprise_member` ADD COLUMN IF NOT EXISTS `invited_by` BIGINT DEFAULT NULL COMMENT '邀请人成员ID';

-- 2. 入企申请表
CREATE TABLE IF NOT EXISTS `enterprise_join_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `enterprise_id` BIGINT NOT NULL COMMENT '企业ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '申请用户ID（游客申请为空）',
  `name` VARCHAR(50) NOT NULL COMMENT '申请人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `note` VARCHAR(500) DEFAULT NULL COMMENT '申请留言',
  `apply_role` VARCHAR(30) NOT NULL DEFAULT 'STAFF' COMMENT '申请角色 OWNER/ADMIN/STAFF',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/AGREED/REFUSED',
  `source` VARCHAR(50) DEFAULT '成员邀请链接' COMMENT '申请来源',
  `handled_by` BIGINT DEFAULT NULL COMMENT '处理人',
  `create_by` BIGINT DEFAULT NULL,
  `date` DATE DEFAULT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_eja_enterprise_status` (`enterprise_id`,`status`),
  KEY `idx_eja_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入企申请';

-- 3. 企业邀请记录表
CREATE TABLE IF NOT EXISTS `enterprise_invite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `enterprise_id` BIGINT NOT NULL COMMENT '企业ID',
  `inviter_id` BIGINT NOT NULL COMMENT '邀请人用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '被邀请人手机号',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '被邀请人姓名',
  `invite_role` VARCHAR(30) NOT NULL DEFAULT 'STAFF' COMMENT '邀请角色 OWNER/ADMIN/STAFF',
  `invite_code` VARCHAR(40) DEFAULT NULL COMMENT '邀请码',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACCEPTED/EXPIRED',
  `accepted_at` TIMESTAMP NULL DEFAULT NULL COMMENT '接受时间',
  `create_by` BIGINT DEFAULT NULL,
  `date` DATE DEFAULT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enterprise_invite_code` (`invite_code`),
  KEY `idx_ei_enterprise_status` (`enterprise_id`,`status`),
  KEY `idx_ei_inviter` (`inviter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业邀请记录';

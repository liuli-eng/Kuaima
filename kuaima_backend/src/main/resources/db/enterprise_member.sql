CREATE TABLE IF NOT EXISTS `enterprise` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `company_code` VARCHAR(32) NOT NULL,
  `company_name` VARCHAR(120) NOT NULL,
  `license_no` VARCHAR(50) DEFAULT NULL,
  `legal_rep` VARCHAR(50) DEFAULT NULL,
  `industry` VARCHAR(50) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `create_by` BIGINT DEFAULT NULL,
  `date` DATE DEFAULT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enterprise_company_code` (`company_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业主体';

CREATE TABLE IF NOT EXISTS `enterprise_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `enterprise_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `member_role` VARCHAR(30) NOT NULL DEFAULT 'STAFF',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `permissions` TEXT DEFAULT NULL,
  `create_by` BIGINT DEFAULT NULL,
  `date` DATE DEFAULT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enterprise_member` (`enterprise_id`, `user_id`),
  KEY `idx_enterprise_member_user` (`user_id`, `status`),
  KEY `idx_enterprise_member_enterprise` (`enterprise_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业成员关系';

-- 将现有已通过企业认证的账号迁移为企业 OWNER。重复执行安全。
INSERT INTO `enterprise` (`company_code`, `company_name`, `license_no`, `legal_rep`, `industry`, `status`, `create_by`)
SELECT u.`company_code`, COALESCE(NULLIF(u.`company_name`, ''), CONCAT('企业', u.`id`)),
       u.`license_no`, u.`legal_rep`, u.`industry`, 'ACTIVE', u.`id`
FROM `sys_user` u
WHERE u.`company_code` IS NOT NULL AND u.`company_code` <> ''
  AND (u.`enterprise_status` = 'APPROVED'
       OR (UPPER(COALESCE(u.`cert_type`, '')) = 'ENTERPRISE' AND u.`cert_status` = '已通过'))
  AND NOT EXISTS (SELECT 1 FROM `enterprise` e WHERE e.`company_code` = u.`company_code`);

INSERT INTO `enterprise_member` (`enterprise_id`, `user_id`, `member_role`, `status`, `create_by`)
SELECT e.`id`, u.`id`, 'OWNER', 'ACTIVE', u.`id`
FROM `sys_user` u JOIN `enterprise` e ON e.`company_code` = u.`company_code`
WHERE u.`company_code` IS NOT NULL AND u.`company_code` <> ''
  AND (u.`enterprise_status` = 'APPROVED'
       OR (UPPER(COALESCE(u.`cert_type`, '')) = 'ENTERPRISE' AND u.`cert_status` = '已通过'))
  AND NOT EXISTS (SELECT 1 FROM `enterprise_member` m
                  WHERE m.`enterprise_id` = e.`id` AND m.`user_id` = u.`id`);

-- 资源表先增加企业归属字段，历史数据按创建人/老板账号回填。
ALTER TABLE `boss_order` ADD COLUMN IF NOT EXISTS `enterprise_id` BIGINT NULL;
ALTER TABLE `boss_address` ADD COLUMN IF NOT EXISTS `enterprise_id` BIGINT NULL;
ALTER TABLE `boss_recruit_settings` ADD COLUMN IF NOT EXISTS `enterprise_id` BIGINT NULL;
ALTER TABLE `boss_order_template` ADD COLUMN IF NOT EXISTS `enterprise_id` BIGINT NULL;
ALTER TABLE `boss_attendance_code` ADD COLUMN IF NOT EXISTS `enterprise_id` BIGINT NULL;

-- 企业统一考勤码按企业+日期唯一；旧的老板+日期唯一约束会导致企业成员刷新时重复键。
SET @attendance_old_unique = (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'boss_attendance_code'
    AND index_name = 'uk_boss_attendance_code_day'
);
SET @attendance_drop_sql = IF(@attendance_old_unique > 0,
  'ALTER TABLE `boss_attendance_code` DROP INDEX `uk_boss_attendance_code_day`', 'SELECT 1');
PREPARE attendance_drop_stmt FROM @attendance_drop_sql;
EXECUTE attendance_drop_stmt;
DEALLOCATE PREPARE attendance_drop_stmt;
SET @attendance_enterprise_unique = (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'boss_attendance_code'
    AND index_name = 'uk_boss_attendance_enterprise_day'
);
SET @attendance_add_sql = IF(@attendance_enterprise_unique = 0,
  'ALTER TABLE `boss_attendance_code` ADD UNIQUE KEY `uk_boss_attendance_enterprise_day` (`enterprise_id`, `code_date`)', 'SELECT 1');
PREPARE attendance_add_stmt FROM @attendance_add_sql;
EXECUTE attendance_add_stmt;
DEALLOCATE PREPARE attendance_add_stmt;

UPDATE `boss_order` o JOIN `enterprise_member` m ON m.`user_id` = o.`create_by` AND m.`member_role` = 'OWNER'
SET o.`enterprise_id` = m.`enterprise_id` WHERE o.`enterprise_id` IS NULL;
UPDATE `boss_address` a JOIN `enterprise_member` m ON m.`user_id` = a.`user_id` AND m.`member_role` = 'OWNER'
SET a.`enterprise_id` = m.`enterprise_id` WHERE a.`enterprise_id` IS NULL;
UPDATE `boss_recruit_settings` s JOIN `enterprise_member` m ON m.`user_id` = s.`boss_id` AND m.`member_role` = 'OWNER'
SET s.`enterprise_id` = m.`enterprise_id` WHERE s.`enterprise_id` IS NULL;
UPDATE `boss_order_template` t JOIN `enterprise_member` m ON m.`user_id` = t.`owner_user_id` AND m.`member_role` = 'OWNER'
SET t.`enterprise_id` = m.`enterprise_id` WHERE t.`enterprise_id` IS NULL;
UPDATE `boss_attendance_code` c JOIN `enterprise_member` m ON m.`user_id` = c.`boss_id` AND m.`member_role` = 'OWNER'
SET c.`enterprise_id` = m.`enterprise_id` WHERE c.`enterprise_id` IS NULL;

-- 索引可在正式迁移窗口单独创建；部分 MySQL 版本不支持 CREATE INDEX IF NOT EXISTS。

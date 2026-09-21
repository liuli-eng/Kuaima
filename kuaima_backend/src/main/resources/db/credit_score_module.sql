-- 信用分/零工星级分结构化字段迁移
-- JPA ddl-auto=update 环境会自动补齐；其他环境发布前执行此脚本。
--
-- MySQL（尤其 8.0.28 及更早版本）不支持
-- ALTER TABLE ... ADD COLUMN IF NOT EXISTS 和 CREATE INDEX IF NOT EXISTS，
-- 因此这里统一通过 information_schema 判断后再执行动态 DDL。

DROP PROCEDURE IF EXISTS `migrate_credit_score_module`;

DELIMITER $$

CREATE PROCEDURE `migrate_credit_score_module`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'star_score'
  ) THEN
    ALTER TABLE `sys_user` ADD COLUMN `star_score` INT DEFAULT 0;
  END IF;

  ALTER TABLE `sys_user` MODIFY COLUMN `credit_score` INT DEFAULT 0;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_flow' AND COLUMN_NAME = 'score_type'
  ) THEN
    ALTER TABLE `credit_flow` ADD COLUMN `score_type` VARCHAR(40);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_flow' AND COLUMN_NAME = 'idempotency_key'
  ) THEN
    ALTER TABLE `credit_flow` ADD COLUMN `idempotency_key` VARCHAR(80);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_flow' AND COLUMN_NAME = 'before_score'
  ) THEN
    ALTER TABLE `credit_flow` ADD COLUMN `before_score` INT;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_flow' AND COLUMN_NAME = 'after_score'
  ) THEN
    ALTER TABLE `credit_flow` ADD COLUMN `after_score` INT;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_flow' AND COLUMN_NAME = 'rule_code'
  ) THEN
    ALTER TABLE `credit_flow` ADD COLUMN `rule_code` VARCHAR(80);
  END IF;

  UPDATE `sys_user` SET `star_score` = 0 WHERE `star_score` IS NULL;
  UPDATE `sys_user` SET `credit_score` = 0 WHERE `credit_score` IS NULL;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_flow'
      AND INDEX_NAME = 'uk_credit_flow_idempotency_key'
  ) THEN
    ALTER TABLE `credit_flow`
      ADD UNIQUE KEY `uk_credit_flow_idempotency_key` (`idempotency_key`);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_flow'
      AND INDEX_NAME = 'idx_credit_flow_user_score_type'
  ) THEN
    ALTER TABLE `credit_flow`
      ADD KEY `idx_credit_flow_user_score_type` (`user_id`, `score_type`, `timestamp`);
  END IF;
END$$

DELIMITER ;

CALL `migrate_credit_score_module`();
DROP PROCEDURE `migrate_credit_score_module`;

-- 老板岗位列表高级筛选所需字段。
-- 适用：MySQL 8.x。脚本通过 information_schema 判断字段和索引，可重复执行。

DROP PROCEDURE IF EXISTS `add_boss_order_filter_fields`;

DELIMITER $$

CREATE PROCEDURE `add_boss_order_filter_fields`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'job_category_id'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `job_category_id` bigint DEFAULT NULL COMMENT '工种分类ID' AFTER `trial_duration`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'longitude'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `longitude` decimal(10,7) DEFAULT NULL COMMENT '工作地点经度' AFTER `job_category_id`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'latitude'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `latitude` decimal(10,7) DEFAULT NULL COMMENT '工作地点纬度' AFTER `longitude`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'experience'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `experience` varchar(30) DEFAULT NULL COMMENT '经验要求编码' AFTER `latitude`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'gender'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `gender` varchar(20) DEFAULT NULL COMMENT '性别要求编码' AFTER `experience`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND INDEX_NAME = 'idx_boss_order_owner_status'
  ) THEN
    CREATE INDEX `idx_boss_order_owner_status` ON `boss_order` (`create_by`, `order_status`);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND INDEX_NAME = 'idx_boss_order_start_time'
  ) THEN
    CREATE INDEX `idx_boss_order_start_time` ON `boss_order` (`start_time`);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND INDEX_NAME = 'idx_boss_order_job_category'
  ) THEN
    CREATE INDEX `idx_boss_order_job_category` ON `boss_order` (`job_category_id`);
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND INDEX_NAME = 'idx_boss_order_salary'
  ) THEN
    CREATE INDEX `idx_boss_order_salary` ON `boss_order` (`salary`);
  END IF;
END$$

DELIMITER ;

CALL `add_boss_order_filter_fields`();
DROP PROCEDURE `add_boss_order_filter_fields`;

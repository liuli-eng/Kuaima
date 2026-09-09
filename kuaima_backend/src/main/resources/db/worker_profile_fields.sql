-- 零工个人资料页扩展字段。
-- 适用：MySQL 8.x，可重复执行。

DROP PROCEDURE IF EXISTS `add_worker_profile_fields`;

DELIMITER $$

CREATE PROCEDURE `add_worker_profile_fields`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'birthday'
  ) THEN
    ALTER TABLE `sys_user` ADD COLUMN `birthday` date DEFAULT NULL COMMENT '出生日期';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'work_years'
  ) THEN
    ALTER TABLE `sys_user` ADD COLUMN `work_years` int DEFAULT NULL COMMENT '工作年限';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'accept_night_shift'
  ) THEN
    ALTER TABLE `sys_user` ADD COLUMN `accept_night_shift` bit(1) DEFAULT NULL COMMENT '是否接受夜班';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'introduction'
  ) THEN
    ALTER TABLE `sys_user` ADD COLUMN `introduction` varchar(500) DEFAULT NULL COMMENT '个人简介';
  END IF;
END$$

DELIMITER ;

CALL `add_worker_profile_fields`();
DROP PROCEDURE `add_worker_profile_fields`;

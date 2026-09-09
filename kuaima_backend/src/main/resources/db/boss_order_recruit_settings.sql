-- BUG-005：岗位招工设置字段迁移。
-- MySQL 8.x，可重复执行。历史岗位按页面原有默认值补齐。

DROP PROCEDURE IF EXISTS `add_boss_order_recruit_settings`;

DELIMITER $$

CREATE PROCEDURE `add_boss_order_recruit_settings`()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'sign_mode'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `sign_mode` varchar(10) DEFAULT NULL COMMENT '报名方式：auto自动通过/manual手动审核';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'phone_notify'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `phone_notify` bit(1) DEFAULT NULL COMMENT '报名电话通知';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'sign_notify'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `sign_notify` bit(1) DEFAULT NULL COMMENT '零工报名通知';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'start_remind'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `start_remind` bit(1) DEFAULT NULL COMMENT '开工提醒';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_order' AND COLUMN_NAME = 'settle_notify'
  ) THEN
    ALTER TABLE `boss_order`
      ADD COLUMN `settle_notify` bit(1) DEFAULT NULL COMMENT '结算通知';
  END IF;

  UPDATE `boss_order`
  SET `sign_mode` = COALESCE(NULLIF(`sign_mode`, ''), 'auto'),
      `phone_notify` = COALESCE(`phone_notify`, b'1'),
      `sign_notify` = COALESCE(`sign_notify`, b'1'),
      `start_remind` = COALESCE(`start_remind`, b'1'),
      `settle_notify` = COALESCE(`settle_notify`, b'1')
  WHERE `sign_mode` IS NULL OR `sign_mode` = ''
     OR `phone_notify` IS NULL OR `sign_notify` IS NULL
     OR `start_remind` IS NULL OR `settle_notify` IS NULL;
END$$

DELIMITER ;

CALL `add_boss_order_recruit_settings`();
DROP PROCEDURE `add_boss_order_recruit_settings`;

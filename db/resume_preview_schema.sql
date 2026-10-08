-- =====================================================================
-- 快马日结 - 简历预览图字段
-- 场景：PDF 简历由后端渲染首页为 PNG 存 OSS，小程序直接当图片预览原件。
-- 适用：MySQL 8.x（MySQL 不支持 ADD COLUMN IF NOT EXISTS，用 information_schema 判断实现幂等）
-- =====================================================================

DROP PROCEDURE IF EXISTS sp_boss_resume_preview;

DELIMITER $$
CREATE PROCEDURE sp_boss_resume_preview()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.TABLES
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'boss_resume') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'boss_resume' AND COLUMN_NAME = 'preview_url') THEN
            ALTER TABLE `boss_resume` ADD COLUMN `preview_url` VARCHAR(500) DEFAULT NULL
                COMMENT '原件的可视化预览图（PDF 首页渲染的 PNG），非 PDF 或渲染失败时为空' AFTER `file_url`;
        END IF;
    END IF;
END$$
DELIMITER ;

CALL sp_boss_resume_preview();
DROP PROCEDURE IF EXISTS sp_boss_resume_preview;
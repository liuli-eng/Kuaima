SET @finish_at_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'boss_order_item' AND column_name = 'finish_at'
);
SET @finish_at_sql = IF(@finish_at_exists = 0,
    'ALTER TABLE boss_order_item ADD COLUMN finish_at DATETIME NULL COMMENT ''确认完工时间，供24小时结算率精确统计'' AFTER finish_date',
    'SELECT 1');
PREPARE finish_at_statement FROM @finish_at_sql;
EXECUTE finish_at_statement;
DEALLOCATE PREPARE finish_at_statement;

-- 历史数据只有日期，按当天零点近似回填；新数据由后端记录精确到秒的完工确认时间。
UPDATE boss_order_item
SET finish_at = TIMESTAMP(finish_date)
WHERE finish_at IS NULL AND finish_date IS NOT NULL;

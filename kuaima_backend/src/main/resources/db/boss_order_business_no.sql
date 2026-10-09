-- 对外业务订单号：后台展示和检索使用 order_no，不暴露 boss_order.id。
SET @order_no_column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'boss_order' AND column_name = 'order_no'
);
SET @order_no_column_sql = IF(@order_no_column_exists = 0,
    'ALTER TABLE boss_order ADD COLUMN order_no VARCHAR(40) NULL AFTER order_title',
    'SELECT 1');
PREPARE order_no_column_statement FROM @order_no_column_sql;
EXECUTE order_no_column_statement;
DEALLOCATE PREPARE order_no_column_statement;

-- 为历史订单按业务日期生成 DDyyyyMMdd0001 格式的递增编号。
UPDATE boss_order o
JOIN (
    SELECT id,
           CONCAT(
               'DD',
               DATE_FORMAT(COALESCE(`date`, CURRENT_DATE), '%Y%m%d'),
               LPAD(ROW_NUMBER() OVER (
                   PARTITION BY COALESCE(`date`, CURRENT_DATE)
                   ORDER BY id
               ), 4, '0')) AS generated_order_no
    FROM boss_order
) numbered ON numbered.id = o.id
SET o.order_no = numbered.generated_order_no
WHERE o.order_no IS NULL OR o.order_no = '';

-- 新订单由 BossOrder @PrePersist 自动生成；历史回填后保证业务编号唯一。
SET @order_no_index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'boss_order' AND index_name = 'uk_boss_order_order_no'
);
SET @order_no_index_sql = IF(@order_no_index_exists = 0,
    'ALTER TABLE boss_order ADD UNIQUE KEY uk_boss_order_order_no (order_no)',
    'SELECT 1');
PREPARE order_no_index_statement FROM @order_no_index_sql;
EXECUTE order_no_index_statement;
DEALLOCATE PREPARE order_no_index_statement;

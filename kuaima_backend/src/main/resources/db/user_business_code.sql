SET @worker_code_column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'worker_code'
);
SET @worker_code_column_sql = IF(
    @worker_code_column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN worker_code VARCHAR(8) NULL COMMENT ''零工对外业务编号（G+7位数字）''',
    'SELECT 1'
);
PREPARE worker_code_column_statement FROM @worker_code_column_sql;
EXECUTE worker_code_column_statement;
DEALLOCATE PREPARE worker_code_column_statement;

SET @boss_code_column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'boss_code'
);
SET @boss_code_column_sql = IF(
    @boss_code_column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN boss_code VARCHAR(8) NULL COMMENT ''老板对外业务编号（B+7位数字）''',
    'SELECT 1'
);
PREPARE boss_code_column_statement FROM @boss_code_column_sql;
EXECUTE boss_code_column_statement;
DEALLOCATE PREPARE boss_code_column_statement;

-- 历史数据按独立业务序列补号，不直接暴露或格式化数据库主键。
SET @worker_code_seq = (
    SELECT COALESCE(MAX(CAST(SUBSTRING(worker_code, 2) AS UNSIGNED)), 0) FROM sys_user
);
UPDATE sys_user
SET worker_code = CONCAT('G', LPAD(@worker_code_seq := @worker_code_seq + 1, 7, '0'))
WHERE worker_code IS NULL OR worker_code = ''
ORDER BY id;

SET @boss_code_seq = (
    SELECT COALESCE(MAX(CAST(SUBSTRING(boss_code, 2) AS UNSIGNED)), 0) FROM sys_user
);
UPDATE sys_user
SET boss_code = CONCAT('B', LPAD(@boss_code_seq := @boss_code_seq + 1, 7, '0'))
WHERE (UPPER(COALESCE(enterprise_status, '')) = 'APPROVED'
       OR (UPPER(COALESCE(cert_type, '')) = 'ENTERPRISE' AND cert_status = '已通过'))
  AND (boss_code IS NULL OR boss_code = '')
ORDER BY id;

SET @worker_code_index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'sys_user'
      AND column_name = 'worker_code' AND non_unique = 0
);
SET @worker_code_index_sql = IF(
    @worker_code_index_exists = 0,
    'ALTER TABLE sys_user ADD UNIQUE KEY uk_sys_user_worker_code (worker_code)',
    'SELECT 1'
);
PREPARE worker_code_index_statement FROM @worker_code_index_sql;
EXECUTE worker_code_index_statement;
DEALLOCATE PREPARE worker_code_index_statement;

SET @boss_code_index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'sys_user'
      AND column_name = 'boss_code' AND non_unique = 0
);
SET @boss_code_index_sql = IF(
    @boss_code_index_exists = 0,
    'ALTER TABLE sys_user ADD UNIQUE KEY uk_sys_user_boss_code (boss_code)',
    'SELECT 1'
);
PREPARE boss_code_index_statement FROM @boss_code_index_sql;
EXECUTE boss_code_index_statement;
DEALLOCATE PREPARE boss_code_index_statement;

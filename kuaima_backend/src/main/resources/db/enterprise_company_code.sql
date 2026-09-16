ALTER TABLE sys_user
    ADD COLUMN company_code VARCHAR(32) NULL COMMENT '企业认证通过后生成的唯一运营编号';

UPDATE sys_user
SET company_code = CONCAT('ENT', LPAD(id, 8, '0'))
WHERE (enterprise_status = 'APPROVED'
       OR (UPPER(COALESCE(cert_type, '')) = 'ENTERPRISE' AND cert_status = '已通过'))
  AND (company_code IS NULL OR company_code = '');

SET @company_code_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND column_name = 'company_code'
      AND non_unique = 0
);
SET @company_code_index_sql = IF(
    @company_code_index_exists = 0,
    'ALTER TABLE sys_user ADD UNIQUE KEY uk_sys_user_company_code (company_code)',
    'SELECT 1'
);
PREPARE company_code_statement FROM @company_code_index_sql;
EXECUTE company_code_statement;
DEALLOCATE PREPARE company_code_statement;

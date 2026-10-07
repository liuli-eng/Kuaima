-- 清理历史上“零工业务身份却使用老板默认昵称”的数据。
-- 执行前请先只读确认候选数据；脚本只处理未通过企业认证且昵称严格匹配“老板+数字”的记录。
-- 业务身份判定：enterprise_status=APPROVED，或 cert_type=ENTERPRISE 且 cert_status=已通过，才是老板。

START TRANSACTION;

-- 预览待处理记录：
SELECT id, role, enterprise_status, cert_type, cert_status, nickname
FROM sys_user
WHERE NOT (
    UPPER(COALESCE(enterprise_status, '')) = 'APPROVED'
    OR (UPPER(COALESCE(cert_type, '')) = 'ENTERPRISE' AND cert_status = '已通过')
  )
  AND nickname REGEXP '^老板[0-9]+$';

-- 使用用户主键生成稳定且不重复的历史昵称，避免批量迁移时出现“零工01”冲突。
UPDATE sys_user
SET nickname = CONCAT('零工', id)
WHERE NOT (
    UPPER(COALESCE(enterprise_status, '')) = 'APPROVED'
    OR (UPPER(COALESCE(cert_type, '')) = 'ENTERPRISE' AND cert_status = '已通过')
  )
  AND nickname REGEXP '^老板[0-9]+$';

-- 核对影响后提交；如需人工复核，请执行 ROLLBACK 而不是 COMMIT。
COMMIT;

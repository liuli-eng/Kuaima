ALTER TABLE admin_rules ADD COLUMN IF NOT EXISTS creator VARCHAR(50) NULL AFTER content;
ALTER TABLE admin_rules ADD COLUMN IF NOT EXISTS rule_type VARCHAR(20) NULL AFTER category;

UPDATE admin_rules SET creator = '管理员' WHERE creator IS NULL OR creator = '';
UPDATE admin_rules
SET rule_type = CASE WHEN category = '信用分规则' THEN 'credit' ELSE 'platform' END
WHERE rule_type IS NULL OR rule_type = '';

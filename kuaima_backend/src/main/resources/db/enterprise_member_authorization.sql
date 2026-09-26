-- 企业员工菜单授权字段迁移。员工仍复用 sys_user，仅增加企业成员关系的授权状态和权限。
ALTER TABLE enterprise_member
    ADD COLUMN IF NOT EXISTS portal_enabled TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS authorized_by BIGINT NULL,
    ADD COLUMN IF NOT EXISTS authorized_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS permission_version INT NOT NULL DEFAULT 1;

-- 企业 OWNER 历史数据默认可以进入企业端；普通成员需经过老板授权。
UPDATE enterprise_member
SET portal_enabled = 1
WHERE member_role = 'OWNER' AND (portal_enabled = 0 OR portal_enabled IS NULL);

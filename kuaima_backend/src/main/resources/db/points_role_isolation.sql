-- 积分账户与流水按“用户 + 身份”隔离：
-- BOSS = 老板身份积分，USER = 零工身份积分。

SET @account_role_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'points_account'
      AND COLUMN_NAME = 'role'
);
SET @account_role_column_sql = IF(
    @account_role_column_exists = 0,
    'ALTER TABLE points_account ADD COLUMN role VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE account_role_column_stmt FROM @account_role_column_sql;
EXECUTE account_role_column_stmt;
DEALLOCATE PREPARE account_role_column_stmt;

-- 历史账户只有一个余额，无法安全拆成两份：
-- 1) 有购买/赠送支出等老板侧流水的，迁移到 BOSS 身份；
-- 2) 仅收到赠送的，迁移到 USER 身份；
-- 3) 无流水时按用户当前身份迁移；
-- 4) 其余默认 BOSS。另一个身份首次访问时自动创建 0 余额账户。
UPDATE points_account a
LEFT JOIN sys_user u ON u.id = a.user_id
SET a.role = CASE
    WHEN EXISTS (
        SELECT 1 FROM points_flow f
        WHERE f.user_id = a.user_id
          AND f.biz_type IN ('PURCHASE', 'ADMIN_PURCHASE', 'GIFT', 'CONSUME')
    ) THEN 'BOSS'
    WHEN EXISTS (
        SELECT 1 FROM points_flow f
        WHERE f.user_id = a.user_id
          AND f.biz_type = 'RECEIVED'
    ) THEN 'USER'
    WHEN u.role IN ('BOSS', 'USER') THEN u.role
    ELSE 'BOSS'
END
WHERE a.role IS NULL OR a.role NOT IN ('BOSS', 'USER');

ALTER TABLE points_account MODIFY role VARCHAR(20) NOT NULL;

-- 删除旧的单用户唯一索引，为用户+身份唯一约束让位。
SET @old_account_user_unique = (
    SELECT INDEX_NAME
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'points_account'
      AND INDEX_NAME <> 'PRIMARY'
    GROUP BY INDEX_NAME
    HAVING COUNT(1) = 1
       AND SUM(COLUMN_NAME = 'user_id') = 1
    LIMIT 1
);
SET @old_account_user_unique_sql = IF(
    @old_account_user_unique IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE points_account DROP INDEX ', @old_account_user_unique)
);
PREPARE old_account_user_unique_stmt FROM @old_account_user_unique_sql;
EXECUTE old_account_user_unique_stmt;
DEALLOCATE PREPARE old_account_user_unique_stmt;

SET @account_user_role_unique_exists = (
    SELECT COUNT(DISTINCT INDEX_NAME)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'points_account'
      AND INDEX_NAME = 'uk_points_account_user_role'
);
SET @account_user_role_unique_sql = IF(
    @account_user_role_unique_exists = 0,
    'ALTER TABLE points_account ADD CONSTRAINT uk_points_account_user_role UNIQUE (user_id, role)',
    'SELECT 1'
);
PREPARE account_user_role_unique_stmt FROM @account_user_role_unique_sql;
EXECUTE account_user_role_unique_stmt;
DEALLOCATE PREPARE account_user_role_unique_stmt;

SET @flow_role_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'points_flow'
      AND COLUMN_NAME = 'role'
);
SET @flow_role_column_sql = IF(
    @flow_role_column_exists = 0,
    'ALTER TABLE points_flow ADD COLUMN role VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE flow_role_column_stmt FROM @flow_role_column_sql;
EXECUTE flow_role_column_stmt;
DEALLOCATE PREPARE flow_role_column_stmt;

-- 老板收到积分的流水暂不存在；历史 RECEIVED 流水归零工身份，其余归老板身份。
UPDATE points_flow
SET role = CASE WHEN biz_type = 'RECEIVED' THEN 'USER' ELSE 'BOSS' END
WHERE role IS NULL OR role NOT IN ('BOSS', 'USER');

ALTER TABLE points_flow MODIFY role VARCHAR(20) NOT NULL;

SET @flow_user_role_index_exists = (
    SELECT COUNT(DISTINCT INDEX_NAME)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'points_flow'
      AND INDEX_NAME = 'idx_points_flow_user_role_time'
);
SET @flow_user_role_index_sql = IF(
    @flow_user_role_index_exists = 0,
    'CREATE INDEX idx_points_flow_user_role_time ON points_flow (user_id, role, `timestamp`)',
    'SELECT 1'
);
PREPARE flow_user_role_index_stmt FROM @flow_user_role_index_sql;
EXECUTE flow_user_role_index_stmt;
DEALLOCATE PREPARE flow_user_role_index_stmt;

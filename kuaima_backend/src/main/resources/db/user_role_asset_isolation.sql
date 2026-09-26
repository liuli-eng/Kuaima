-- 同一用户的老板/零工资产隔离迁移（MySQL 8）。
-- 删除仅包含 user_id 的旧唯一索引，建立 user_id + role 联合唯一索引。

ALTER TABLE wallet ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER user_id;
ALTER TABLE wallet_flow ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER user_id;
ALTER TABLE with_draw ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER user_id;

SET @wallet_old_unique = (
    SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'wallet' AND non_unique = 0
    GROUP BY index_name
    HAVING COUNT(*) = 1 AND MAX(column_name) = 'user_id'
    LIMIT 1
);
SET @wallet_drop_sql = IF(@wallet_old_unique IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE wallet DROP INDEX `', REPLACE(@wallet_old_unique, '`', '``'), '`'));
PREPARE wallet_drop_stmt FROM @wallet_drop_sql;
EXECUTE wallet_drop_stmt;
DEALLOCATE PREPARE wallet_drop_stmt;

SET @wallet_role_unique = (
    SELECT COUNT(*) FROM (
        SELECT index_name FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'wallet' AND non_unique = 0
          AND column_name IN ('user_id', 'role')
        GROUP BY index_name HAVING COUNT(*) = 2
    ) indexes_found
);
SET @wallet_add_sql = IF(@wallet_role_unique > 0, 'SELECT 1',
    'ALTER TABLE wallet ADD UNIQUE KEY uk_wallet_user_role (user_id, role)');
PREPARE wallet_add_stmt FROM @wallet_add_sql;
EXECUTE wallet_add_stmt;
DEALLOCATE PREPARE wallet_add_stmt;

ALTER TABLE reward_account ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER user_id;
ALTER TABLE reward_flow ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER user_id;
ALTER TABLE reward_withdrawal ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER user_id;
ALTER TABLE reward_recharge_order ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'BOSS' AFTER user_id;

SET @reward_old_unique = (
    SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'reward_account' AND non_unique = 0
    GROUP BY index_name
    HAVING COUNT(*) = 1 AND MAX(column_name) = 'user_id'
    LIMIT 1
);
SET @reward_drop_sql = IF(@reward_old_unique IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE reward_account DROP INDEX `', REPLACE(@reward_old_unique, '`', '``'), '`'));
PREPARE reward_drop_stmt FROM @reward_drop_sql;
EXECUTE reward_drop_stmt;
DEALLOCATE PREPARE reward_drop_stmt;

SET @reward_role_unique = (
    SELECT COUNT(*) FROM (
        SELECT index_name FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'reward_account' AND non_unique = 0
          AND column_name IN ('user_id', 'role')
        GROUP BY index_name HAVING COUNT(*) = 2
    ) indexes_found
);
SET @reward_add_sql = IF(@reward_role_unique > 0, 'SELECT 1',
    'ALTER TABLE reward_account ADD UNIQUE KEY uk_reward_account_user_role (user_id, role)');
PREPARE reward_add_stmt FROM @reward_add_sql;
EXECUTE reward_add_stmt;
DEALLOCATE PREPARE reward_add_stmt;

UPDATE reward_recharge_order SET role = 'BOSS' WHERE role IS NULL OR role = '';

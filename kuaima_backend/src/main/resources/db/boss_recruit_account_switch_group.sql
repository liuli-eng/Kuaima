-- 老板端“更换账号”的可切换账号组。
-- owner_user_id 是账号组的锚定用户；target_user_id 是可登录的目标老板用户。
CREATE TABLE IF NOT EXISTS boss_recruit_account (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    target_user_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    authorization_type VARCHAR(20) DEFAULT 'PERSONAL',
    work_code VARCHAR(20),
    leave_code VARCHAR(20),
    current BIT(1) DEFAULT b'0',
    `date` DATE,
    create_by BIGINT,
    `timestamp` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 兼容旧表：动态补充目标用户列，避免重复执行时报列已存在。
SET @target_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'boss_recruit_account'
      AND COLUMN_NAME = 'target_user_id'
);
SET @target_column_sql = IF(
    @target_column_exists = 0,
    'ALTER TABLE boss_recruit_account ADD COLUMN target_user_id BIGINT NULL AFTER owner_user_id',
    'SELECT 1'
);
PREPARE target_column_stmt FROM @target_column_sql;
EXECUTE target_column_stmt;
DEALLOCATE PREPARE target_column_stmt;

UPDATE boss_recruit_account SET target_user_id = owner_user_id WHERE target_user_id IS NULL;
ALTER TABLE boss_recruit_account MODIFY target_user_id BIGINT NOT NULL;

-- 同一账号组内只允许一个目标账号；保留 current 记录和较早记录。
DELETE duplicate_binding
FROM boss_recruit_account duplicate_binding
JOIN (
    SELECT id, ROW_NUMBER() OVER (
               PARTITION BY owner_user_id, target_user_id
               ORDER BY current DESC, id ASC
           ) AS row_no
    FROM boss_recruit_account
) grouped ON grouped.id = duplicate_binding.id
WHERE grouped.row_no > 1;

SET @unique_exists = (
    SELECT COUNT(1)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'boss_recruit_account'
      AND CONSTRAINT_NAME = 'uk_boss_recruit_owner_target'
);
SET @unique_sql = IF(
    @unique_exists = 0,
    'ALTER TABLE boss_recruit_account ADD CONSTRAINT uk_boss_recruit_owner_target UNIQUE (owner_user_id, target_user_id)',
    'SELECT 1'
);
PREPARE unique_stmt FROM @unique_sql;
EXECUTE unique_stmt;
DEALLOCATE PREPARE unique_stmt;

SET @index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'boss_recruit_account'
      AND INDEX_NAME = 'idx_boss_recruit_target_user'
);
SET @index_sql = IF(
    @index_exists = 0,
    'CREATE INDEX idx_boss_recruit_target_user ON boss_recruit_account (target_user_id)',
    'SELECT 1'
);
PREPARE index_stmt FROM @index_sql;
EXECUTE index_stmt;
DEALLOCATE PREPARE index_stmt;

-- 信用分/零工星级分结构化字段迁移
-- JPA ddl-auto=update 环境会自动补齐；其他环境发布前执行此脚本。

ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS star_score INT DEFAULT 0;
ALTER TABLE sys_user MODIFY COLUMN credit_score INT DEFAULT 0;
ALTER TABLE credit_flow ADD COLUMN IF NOT EXISTS score_type VARCHAR(40);
ALTER TABLE credit_flow ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(80);
ALTER TABLE credit_flow ADD COLUMN IF NOT EXISTS before_score INT;
ALTER TABLE credit_flow ADD COLUMN IF NOT EXISTS after_score INT;
ALTER TABLE credit_flow ADD COLUMN IF NOT EXISTS rule_code VARCHAR(80);

UPDATE sys_user SET star_score = 0 WHERE star_score IS NULL;
UPDATE sys_user SET credit_score = 0 WHERE credit_score IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_credit_flow_idempotency_key
    ON credit_flow (idempotency_key);
CREATE INDEX IF NOT EXISTS idx_credit_flow_user_score_type
    ON credit_flow (user_id, score_type, `timestamp`);

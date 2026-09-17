CREATE TABLE IF NOT EXISTS reward_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_reward_account_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reward_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    title VARCHAR(100),
    remark VARCHAR(500),
    biz_type VARCHAR(50),
    biz_id BIGINT,
    source_key VARCHAR(128) NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_reward_flow_source(source_key),
    KEY idx_reward_flow_user_created(user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reward_withdrawal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    applied_at DATETIME NOT NULL,
    paid_at DATETIME,
    failure_reason VARCHAR(500),
    UNIQUE KEY uk_reward_withdrawal_key(idempotency_key),
    KEY idx_reward_withdrawal_user_applied(user_id,applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 将历史上已真实进入钱包的奖励金流水幂等迁移到奖励金子账户。
INSERT IGNORE INTO reward_flow(user_id,type,amount,balance_after,title,remark,biz_type,biz_id,source_key,created_at)
SELECT wf.user_id,'INCOME',wf.amount,0,'平台奖励金',wf.remark,wf.biz_type,wf.biz_id,
       CONCAT('WALLET_FLOW:',wf.id),COALESCE(wf.`timestamp`,CURRENT_TIMESTAMP)
FROM wallet_flow wf
WHERE wf.direction='income' AND wf.biz_type='REWARD';

INSERT INTO reward_account(user_id,balance)
SELECT user_id,SUM(CASE WHEN type='INCOME' THEN amount ELSE -amount END)
FROM reward_flow
GROUP BY user_id
ON DUPLICATE KEY UPDATE balance=VALUES(balance);

UPDATE reward_flow rf
JOIN (
    SELECT id, SUM(IF(type='INCOME',amount,-amount)) OVER (
        PARTITION BY user_id ORDER BY created_at,id ROWS UNBOUNDED PRECEDING
    ) AS calculated_balance
    FROM reward_flow
) calculated ON calculated.id=rf.id
SET rf.balance_after=calculated.calculated_balance;

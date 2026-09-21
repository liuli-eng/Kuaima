CREATE TABLE IF NOT EXISTS worker_review (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    boss_id BIGINT NOT NULL,
    overall_score INT NOT NULL,
    attitude_score INT NOT NULL,
    efficiency_score INT NOT NULL,
    skill_score INT NOT NULL,
    content VARCHAR(200) NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_worker_review_item (item_id),
    KEY idx_worker_review_worker_created (worker_id, created_at),
    KEY idx_worker_review_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老板对零工评价';

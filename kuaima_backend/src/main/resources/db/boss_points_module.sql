-- 积分套餐统一使用 admin_point_purchase_settings.sql 中的 point_package 表，
-- price 单位为人民币元。旧 points_package 表不再创建或读取。
CREATE TABLE IF NOT EXISTS points_gift (
 id BIGINT NOT NULL AUTO_INCREMENT,idempotency_key VARCHAR(128) NOT NULL,boss_id BIGINT NOT NULL,
 worker_id BIGINT NOT NULL,points INT NOT NULL,create_time DATETIME NOT NULL,PRIMARY KEY(id),
 UNIQUE KEY uk_points_gift_key(idempotency_key),KEY idx_points_gift_boss(boss_id),KEY idx_points_gift_worker(worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

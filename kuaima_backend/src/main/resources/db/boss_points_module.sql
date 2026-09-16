CREATE TABLE IF NOT EXISTS points_package (
 id BIGINT NOT NULL AUTO_INCREMENT, points INT NOT NULL, price INT NOT NULL,
 tag VARCHAR(100), hot BIT(1) DEFAULT 0, enabled BIT(1) DEFAULT 1, PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO points_package(id,points,price,tag,hot,enabled) VALUES
 (1,1000,1000,'适用所有订单',0,1),(2,5000,4500,'省5元 更划算',1,1),
 (3,10000,8000,'省20元',0,1),(4,20000,15000,'省50元',0,1)
ON DUPLICATE KEY UPDATE points=VALUES(points),price=VALUES(price),tag=VALUES(tag),hot=VALUES(hot),enabled=VALUES(enabled);
CREATE TABLE IF NOT EXISTS points_gift (
 id BIGINT NOT NULL AUTO_INCREMENT,idempotency_key VARCHAR(128) NOT NULL,boss_id BIGINT NOT NULL,
 worker_id BIGINT NOT NULL,points INT NOT NULL,create_time DATETIME NOT NULL,PRIMARY KEY(id),
 UNIQUE KEY uk_points_gift_key(idempotency_key),KEY idx_points_gift_boss(boss_id),KEY idx_points_gift_worker(worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

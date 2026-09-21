-- 积分购买订单（JPA ddl-auto=update 会自动维护；此脚本用于已有环境显式建表）
CREATE TABLE IF NOT EXISTS point_purchase_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  boss_id BIGINT NOT NULL,
  boss_name VARCHAR(100),
  company_name VARCHAR(150),
  points BIGINT NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  pay_method VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  purchase_time DATETIME NOT NULL,
  remark VARCHAR(500),
  operator_id BIGINT,
  operator_name VARCHAR(100),
  operator_time DATETIME,
  points_granted BIT(1) DEFAULT 0,
  wechat_transaction_id VARCHAR(64),
  paid_at DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_point_purchase_order_no (order_no),
  UNIQUE KEY uk_point_purchase_idempotency (idempotency_key),
  KEY idx_point_purchase_boss (boss_id),
  KEY idx_point_purchase_status (status),
  KEY idx_point_purchase_method (pay_method),
  KEY idx_point_purchase_time (purchase_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 已有环境升级（MySQL 8 支持 IF NOT EXISTS；JPA ddl-auto=update 也会自动补齐）
ALTER TABLE point_purchase_order ADD COLUMN IF NOT EXISTS wechat_transaction_id VARCHAR(64);
ALTER TABLE point_purchase_order ADD COLUMN IF NOT EXISTS paid_at DATETIME;

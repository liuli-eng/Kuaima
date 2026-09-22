CREATE TABLE IF NOT EXISTS `settlement_payment_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_no` VARCHAR(48) NOT NULL,
  `idempotency_key` VARCHAR(128) NOT NULL,
  `boss_id` BIGINT NOT NULL,
  `settlement_ids` VARCHAR(2000) NOT NULL,
  `amount` DECIMAL(18,2) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `pay_params` VARCHAR(4000) DEFAULT NULL,
  `wechat_transaction_id` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME NOT NULL,
  `paid_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_payment_no` (`payment_no`),
  UNIQUE KEY `uk_settlement_payment_idempotency` (`idempotency_key`),
  KEY `idx_settlement_payment_boss` (`boss_id`, `created_at`),
  KEY `idx_settlement_payment_status` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单结算微信支付单';

-- Settlement 实体新增 wechat_transaction_id 字段；项目的 ddl-auto=update 会自动补齐该字段。

-- 老板钱包微信充值使用原 boss_balance_recharge_order 表，无需新增充值表。
-- 零工钱包微信提现字段。
ALTER TABLE with_draw
    ADD COLUMN idempotency_key VARCHAR(128) NULL,
    ADD COLUMN merchant_batch_no VARCHAR(64) NULL,
    ADD COLUMN merchant_detail_no VARCHAR(64) NULL,
    ADD COLUMN wechat_transfer_no VARCHAR(64) NULL,
    ADD COLUMN transfer_response TEXT NULL,
    ADD UNIQUE KEY uk_wallet_withdraw_idempotency (idempotency_key),
    ADD KEY idx_wallet_withdraw_batch (merchant_batch_no);

ALTER TABLE boss_balance_recharge_order
    ADD COLUMN idempotency_key VARCHAR(128) NULL,
    ADD COLUMN pay_params VARCHAR(4000) NULL,
    ADD UNIQUE KEY uk_bbr_idempotency (idempotency_key);

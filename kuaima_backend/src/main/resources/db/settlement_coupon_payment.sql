-- 结算支付单记录优惠券快照，支付回调成功后幂等核销。
ALTER TABLE settlement_payment_order
    ADD COLUMN IF NOT EXISTS user_coupon_id BIGINT NULL AFTER amount,
    ADD COLUMN IF NOT EXISTS coupon_deduct_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 AFTER user_coupon_id;

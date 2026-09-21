-- 招工模板创建时保存关联订单已邀请零工 ID 的 JSON 数组快照。
ALTER TABLE boss_order_template
    ADD COLUMN IF NOT EXISTS invited_worker_ids VARCHAR(2000);

ALTER TABLE boss_order_template
    ADD COLUMN IF NOT EXISTS longitude DECIMAL(10,7),
    ADD COLUMN IF NOT EXISTS latitude DECIMAL(10,7);

-- 招工模板创建时保存关联订单已邀请零工 ID 的 JSON 数组快照。
ALTER TABLE boss_order_template
    ADD COLUMN IF NOT EXISTS invited_worker_ids VARCHAR(2000);

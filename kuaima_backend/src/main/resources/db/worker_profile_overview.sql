ALTER TABLE boss_order_item
    ADD COLUMN IF NOT EXISTS early_leave BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否通过早退流程提前完工';

-- 金额统一为“元”，保留两位小数。
-- 执行前请先备份数据库；本脚本只用于旧库从“分”迁移到“元”，不会被应用启动自动执行。
-- boss_order.salary 历史上已经按元保存，只调整列类型，不做 /100。
ALTER TABLE boss_order MODIFY COLUMN salary DECIMAL(18,2) NULL;

-- 下面字段旧值均按“分”保存，先除以 100，再改为 DECIMAL(18,2)。
UPDATE boss_settlement SET wage = ROUND(wage / 100, 2), service_fee = ROUND(service_fee / 100, 2), total_amount = ROUND(total_amount / 100, 2);
ALTER TABLE boss_settlement MODIFY COLUMN wage DECIMAL(18,2) NULL,
    MODIFY COLUMN service_fee DECIMAL(18,2) NULL,
    MODIFY COLUMN total_amount DECIMAL(18,2) NULL;

UPDATE wallet SET balance = ROUND(balance / 100, 2);
ALTER TABLE wallet MODIFY COLUMN balance DECIMAL(18,2) NULL;
UPDATE wallet_flow SET amount = ROUND(amount / 100, 2), balance_after = ROUND(balance_after / 100, 2);
ALTER TABLE wallet_flow MODIFY COLUMN amount DECIMAL(18,2) NULL,
    MODIFY COLUMN balance_after DECIMAL(18,2) NULL;
UPDATE with_draw SET amount = ROUND(amount / 100, 2);
ALTER TABLE with_draw MODIFY COLUMN amount DECIMAL(18,2) NULL;

UPDATE reward_account SET balance = ROUND(balance / 100, 2);
ALTER TABLE reward_account MODIFY COLUMN balance DECIMAL(18,2) NOT NULL;
UPDATE reward_flow SET amount = ROUND(amount / 100, 2), balance_after = ROUND(balance_after / 100, 2);
ALTER TABLE reward_flow MODIFY COLUMN amount DECIMAL(18,2) NOT NULL,
    MODIFY COLUMN balance_after DECIMAL(18,2) NOT NULL;
UPDATE reward_withdrawal SET amount = ROUND(amount / 100, 2);
ALTER TABLE reward_withdrawal MODIFY COLUMN amount DECIMAL(18,2) NOT NULL;

UPDATE reward_fund_account SET balance = ROUND(balance / 100, 2);
ALTER TABLE reward_fund_account MODIFY COLUMN balance DECIMAL(18,2) NULL;
UPDATE reward_fund_flow SET amount = ROUND(amount / 100, 2), balance_after = ROUND(balance_after / 100, 2);
ALTER TABLE reward_fund_flow MODIFY COLUMN amount DECIMAL(18,2) NOT NULL,
    MODIFY COLUMN balance_after DECIMAL(18,2) NOT NULL;
UPDATE reward_campaign SET amount = ROUND(amount / 100, 2), min_amount = ROUND(min_amount / 100, 2),
    max_amount = ROUND(max_amount / 100, 2), total_amount = ROUND(total_amount / 100, 2),
    actual_amount = ROUND(actual_amount / 100, 2);
ALTER TABLE reward_campaign MODIFY COLUMN amount DECIMAL(18,2) NULL,
    MODIFY COLUMN min_amount DECIMAL(18,2) NULL,
    MODIFY COLUMN max_amount DECIMAL(18,2) NULL,
    MODIFY COLUMN total_amount DECIMAL(18,2) NULL,
    MODIFY COLUMN actual_amount DECIMAL(18,2) NULL;
UPDATE reward_grant SET amount = ROUND(amount / 100, 2);
ALTER TABLE reward_grant MODIFY COLUMN amount DECIMAL(18,2) NULL;

UPDATE payroll_detail SET daily_wage = ROUND(daily_wage / 100, 2), amount = ROUND(amount / 100, 2);
ALTER TABLE payroll_detail MODIFY COLUMN daily_wage DECIMAL(18,2) NULL,
    MODIFY COLUMN amount DECIMAL(18,2) NULL;
UPDATE payroll_order SET amount = ROUND(amount / 100, 2);
ALTER TABLE payroll_order MODIFY COLUMN amount DECIMAL(18,2) NULL;

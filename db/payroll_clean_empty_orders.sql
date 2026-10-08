-- =============================================================================
-- 清理无明细的空发薪单
--
-- 背景：payroll_order 中 id=1/2/3 为测试期产生的空单：
--   标题为测试串（"小姐姐了112出来了" / "测试112" / "测试11"）、
--   amount=0、people_count=0、无任何 payroll_detail 明细、状态 approved，
--   在「发薪管理」列表中显示为 ¥0.00，干扰真实数据查看。
--
-- 判定条件：不存在任何关联明细的薪单（NOT EXISTS payroll_detail）。
--   注意：本条件在删除前会先打印命中行数，请核对是否为预期的 3 条。
-- 幂等：可重复执行；执行后不再有命中行。
-- 回滚：INSERT INTO payroll_order SELECT * FROM payroll_order_empty_bak_20261008;
--       （备份表已保留 id 列，可直接回插）
-- =============================================================================

-- 1. 备份将被删除的空薪单
CREATE TABLE IF NOT EXISTS payroll_order_empty_bak_20261008 AS
SELECT * FROM payroll_order o
WHERE NOT EXISTS (SELECT 1 FROM payroll_detail d WHERE d.payroll_id = o.id);

-- 2. 删除前核对命中范围
SELECT '待删除空薪单' AS item, COUNT(*) AS cnt
FROM payroll_order o
WHERE NOT EXISTS (SELECT 1 FROM payroll_detail d WHERE d.payroll_id = o.id);

SELECT id, order_no, title, amount, people_count, status
FROM payroll_order o
WHERE NOT EXISTS (SELECT 1 FROM payroll_detail d WHERE d.payroll_id = o.id)
ORDER BY id;

-- 3. 执行删除
DELETE o FROM payroll_order o
WHERE NOT EXISTS (SELECT 1 FROM payroll_detail d WHERE d.payroll_id = o.id);

-- 4. 校验：应只剩有明细的薪单，且金额与明细汇总仍自洽
SELECT '剩余薪单数' AS item, COUNT(*) AS cnt FROM payroll_order;

SELECT o.id, o.title, o.amount AS order_amount,
       COALESCE((SELECT SUM(d.amount) FROM payroll_detail d WHERE d.payroll_id = o.id), 0) AS sum_detail,
       o.people_count,
       CASE WHEN o.amount = COALESCE((SELECT SUM(d.amount) FROM payroll_detail d WHERE d.payroll_id = o.id), 0)
            THEN 'OK' ELSE 'MISMATCH' END AS amount_result
FROM payroll_order o ORDER BY o.id;
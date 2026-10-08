-- =====================================================================
-- 清理 boss_merchant_account 的重复种子数据
--
-- 成因：db/boss_balance_schema.sql 里的示例数据 INSERT 早前没有幂等守卫，
--   而该表没有唯一键，于是每次 deploy 都会静默再插一条同一老板的默认账户。
--   线上 boss_id=1001 已累积出多条完全相同的账户，导致：
--     - 「默认账户」不唯一，余额被分散在多行；
--     - 发薪自动扣款按 findFirstByBossIdAndIsDefaultTrueForUpdate 取账户时结果不确定。
--   守卫已在 boss_balance_schema.sql 补上，本脚本负责清理存量。
--
-- 安全设计：
--   1) 先整表快照，可回滚；
--   2) 只删除「boss_id + merchant_no + account_name + balance 完全相同」的多余行，
--      每组保留最小 id；balance 不同（例如被充值改过）的行不会被删。
-- 可重复执行。
-- =====================================================================

-- 1. 备份
CREATE TABLE IF NOT EXISTS `boss_merchant_account_dedup_bak_20261008` AS
SELECT * FROM `boss_merchant_account`;

-- 2. 删除完全重复的种子行，每组保留最小 id
DELETE t FROM `boss_merchant_account` t
JOIN (
    SELECT MIN(`id`) AS keep_id, `boss_id`, `merchant_no`, `account_name`, `balance`
    FROM `boss_merchant_account`
    WHERE `merchant_no` IS NOT NULL
    GROUP BY `boss_id`, `merchant_no`, `account_name`, `balance`
    HAVING COUNT(*) > 1
) d
  ON t.`boss_id` = d.`boss_id`
 AND t.`merchant_no` = d.`merchant_no`
 AND t.`account_name` = d.`account_name`
 AND t.`balance` = d.`balance`
WHERE t.`id` > d.`keep_id`;

-- 3. 校验：清理后应无重复
SELECT '=== 清理后全量 ===' AS s;
SELECT `id`, `boss_id`, `account_name`, `merchant_no`, `balance`, `is_default`, `status`
FROM `boss_merchant_account` ORDER BY `id`;

SELECT '=== 每老板默认账户数（应全为 1） ===' AS s;
SELECT `boss_id`, COUNT(*) AS default_cnt
FROM `boss_merchant_account` WHERE `is_default` = 1
GROUP BY `boss_id` ORDER BY `boss_id`;
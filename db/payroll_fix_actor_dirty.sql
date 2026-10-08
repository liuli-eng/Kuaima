-- =============================================================================
-- 修复 payroll_order.creator / review_by 的操作人脏值
--
-- 现象：字段里存的是 Java 对象的 toString，例如
--   LoginUser[id=1, username=admin, role=ADMIN_SUPER_ADMIN, accountGroupOwnerId=null, ...]
--
-- 根因：PayrollController.operator() 原先用 authentication.getName()。
--   JwtAuthenticationFilter 把 LoginUser（record）作为 principal 放进
--   UsernamePasswordAuthenticationToken；当 principal 不是 UserDetails/Principal 时，
--   getName() 会退化成 principal.toString()，于是整串对象被写进库。
--   代码侧已改为显式取 login.username()，本脚本只负责清理存量脏值。
--
-- 影响范围：payroll_order 中 creator 或 review_by 以 'LoginUser' 开头的行。
-- 幂等：可重复执行；已修好的行不再匹配 LIKE 'LoginUser%'。
-- =============================================================================

-- 1. 备份受影响字段（整表快照，回滚用）
CREATE TABLE IF NOT EXISTS payroll_order_actor_bak_20261008 AS
SELECT id, creator, review_by FROM payroll_order;

-- 2. 从脏字符串里提取 username 回填，而不是写死某个账号名
--    'LoginUser[id=1, username=admin, role=...]' -> 'admin'
UPDATE payroll_order
SET creator = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(creator, 'username=', -1), ',', 1))
WHERE creator LIKE 'LoginUser%' AND creator LIKE '%username=%';

UPDATE payroll_order
SET review_by = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(review_by, 'username=', -1), ',', 1))
WHERE review_by LIKE 'LoginUser%' AND review_by LIKE '%username=%';

-- 3. 校验：应为 0
SELECT '残留 LoginUser 脏值行数' AS item, COUNT(*) AS cnt
FROM payroll_order WHERE creator LIKE 'LoginUser%' OR review_by LIKE 'LoginUser%';

SELECT id, creator, review_by FROM payroll_order ORDER BY id;
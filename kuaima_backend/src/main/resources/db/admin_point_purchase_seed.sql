-- 积分套餐、积分兑换规则、积分获取规则初始化数据
-- 仅在对应表为空时批量插入，保证可重复执行、不破坏已有数据
-- 数据原型对齐 admin/point-purchase.html 544~576 行 pkgData / exchangeData / earnData

INSERT INTO point_package (name, sub, points, price, original_price, save_text, rec, sort, enabled, operator_id, operator_name, created_at, updated_at)
SELECT * FROM (
    SELECT '体验套餐' AS name, '适合首次购买的中小老板' AS sub, 5000 AS points, 50 AS price, NULL AS original_price, NULL AS save_text, FALSE AS rec, 10 AS sort, TRUE AS enabled, NULL AS operator_id, NULL AS operator_name, NOW() AS created_at, NOW() AS updated_at
    UNION ALL SELECT '标准套餐', '日常经营首选，超值性价比', 50000, 480, '¥500', '省 ¥20', TRUE, 20, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '进阶套餐', '适合长期用工的中型企业', 200000, 1800, '¥2,000', '省 ¥200', FALSE, 30, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '企业套餐', '大型企业批量用工专属', 500000, 4200, '¥5,000', '省 ¥800', FALSE, 40, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '旗舰套餐', '集团化企业最优选择', 1000000, 8000, '¥10,000', '省 ¥2,000', FALSE, 50, TRUE, NULL, NULL, NOW(), NOW()
) init
WHERE NOT EXISTS (SELECT 1 FROM point_package);

INSERT INTO point_exchange_rule (name, sub, icon, icon_color, rate, rule_limit, cond, rule_code, formula, sort, enabled, operator_id, operator_name, created_at, updated_at)
SELECT * FROM (
    SELECT '招工发布（免费档）' AS name, '全局适用' AS sub, 'fa-bullhorn' AS icon, '#FF8C5A' AS icon_color, '50 分/天' AS rate, '10 条' AS rule_limit, '基础档位免费，超出按天计费' AS cond, 'JOB_POST_FREE' AS rule_code, NULL AS formula, 10 AS sort, TRUE AS enabled, NULL AS operator_id, NULL AS operator_name, NOW() AS created_at, NOW() AS updated_at
    UNION ALL SELECT '发薪结算', '全局适用', 'fa-money-bill-wave', '#3B82F6', '1 分/元', '无限制', '每笔发薪金额 × 1 积分', 'PAYROLL_SETTLE', NULL, 20, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '考勤打卡核验', '全局适用', 'fa-clipboard-check', '#10B981', '5 分/次', '200 次', '每日核验次数 × 5 积分', 'ATTENDANCE_CHECK', NULL, 30, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '星级认证专属', '条件触发', 'fa-star', '#9333EA', '+20% 加成', '—', '信用分 ≥ 480 自动生效', 'STAR_CERT_BONUS', NULL, 40, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '招聘专场服务费', '全局适用', 'fa-calendar-day', '#F59E0B', '500 分/场', '每月 3 场', '报名专场自动扣费', 'JOB_FAIR_SERVICE', NULL, 50, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '财务报表导出', '全局适用', 'fa-file-invoice', '#3B82F6', '20 分/次', '10 次', '每导出一次扣 20 积分', 'REPORT_EXPORT', NULL, 60, FALSE, NULL, NULL, NOW(), NOW()
) init
WHERE NOT EXISTS (SELECT 1 FROM point_exchange_rule);

INSERT INTO point_earn_rule (name, sub, icon, icon_color, rate, rule_limit, cond, rule_code, formula, sort, enabled, operator_id, operator_name, created_at, updated_at)
SELECT * FROM (
    SELECT '完成日结任务' AS name, '日常任务' AS sub, 'fa-briefcase' AS icon, '#FF8C5A' AS icon_color, '1 分/元' AS rate, '500 分/天' AS rule_limit, '完工确认后按工价自动发放' AS cond, 'DAILY_TASK_FINISH' AS rule_code, NULL AS formula, 10 AS sort, TRUE AS enabled, NULL AS operator_id, NULL AS operator_name, NOW() AS created_at, NOW() AS updated_at
    UNION ALL SELECT '每日签到', '签到任务', 'fa-calendar-check', '#10B981', '5 分/天', '每日 1 次', '签到自动到账，连签 7 天额外 +50 分', 'DAILY_CHECKIN', NULL, 20, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '邀请好友', '邀请拉新', 'fa-user-plus', '#3B82F6', '100 分/人', '每月 20 人', '好友完成首单后到账', 'INVITE_FRIEND', NULL, 30, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '实名认证奖励', '一次性任务', 'fa-id-card', '#9333EA', '200 分/次', '仅 1 次', '完成实名认证后一次性发放', 'REALNAME_BONUS', NULL, 40, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '好评奖励', '互动任务', 'fa-thumbs-up', '#F59E0B', '20 分/次', '每日 3 次', '获得老板 5 星评价后发放', 'POSITIVE_REVIEW', NULL, 50, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '分享岗位', '分享任务', 'fa-share-nodes', '#06B6D4', '10 分/次', '每日 5 次', '好友点击分享链接后到账', 'SHARE_JOB', NULL, 60, TRUE, NULL, NULL, NOW(), NOW()
    UNION ALL SELECT '连续接单加成', '条件触发', 'fa-fire', '#EF4444', '+10% 加成', '—', '本周完成 ≥ 5 单自动生效', 'CONSECUTIVE_ORDERS', NULL, 70, FALSE, NULL, NULL, NOW(), NOW()
) init
WHERE NOT EXISTS (SELECT 1 FROM point_earn_rule);

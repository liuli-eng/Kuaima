package com.kuaima.app.config;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.kuaima.app.admin.entity.PointEarnRule;
import com.kuaima.app.admin.entity.PointExchangeRule;
import com.kuaima.app.admin.entity.PointPackageSetting;
import com.kuaima.app.admin.repository.PointEarnRuleRepository;
import com.kuaima.app.admin.repository.PointExchangeRuleRepository;
import com.kuaima.app.admin.repository.PointPackageSettingRepository;

/**
 * 初始化积分套餐 / 积分兑换规则 / 积分获取规则。
 *
 * <p>与 SQL 原型图 admin/point-purchase.html（L544~L576）保持一致。
 * 仅在对应表为空时写入，保证幂等，不覆盖管理员已配置的数据。
 */
@Component
@Order(100)
public class PointPurchaseSettingsInitializer implements CommandLineRunner {

    private final PointPackageSettingRepository packages;
    private final PointExchangeRuleRepository exchanges;
    private final PointEarnRuleRepository earns;

    public PointPurchaseSettingsInitializer(PointPackageSettingRepository packages,
                                            PointExchangeRuleRepository exchanges,
                                            PointEarnRuleRepository earns) {
        this.packages = packages;
        this.exchanges = exchanges;
        this.earns = earns;
    }

    @Override
    public void run(String... args) {
        seedPackagesIfEmpty();
        seedExchangesIfEmpty();
        seedEarnsIfEmpty();
    }

    private void seedPackagesIfEmpty() {
        if (packages.count() > 0) return;
        packages.saveAll(List.of(
                packageRow("体验套餐", "适合首次购买的中小老板", 5000L, 50L, null, null, false, 10),
                packageRow("标准套餐", "日常经营首选，超值性价比", 50000L, 480L, "¥500", "省 ¥20", true, 20),
                packageRow("进阶套餐", "适合长期用工的中型企业", 200000L, 1800L, "¥2,000", "省 ¥200", false, 30),
                packageRow("企业套餐", "大型企业批量用工专属", 500000L, 4200L, "¥5,000", "省 ¥800", false, 40),
                packageRow("旗舰套餐", "集团化企业最优选择", 1000000L, 8000L, "¥10,000", "省 ¥2,000", false, 50)
        ));
    }

    private void seedExchangesIfEmpty() {
        if (exchanges.count() > 0) return;
        exchanges.saveAll(List.of(
                exchangeRow("招工发布（免费档）", "全局适用", "fa-bullhorn", "#FF8C5A",
                        "50 分/天", "10 条", "基础档位免费，超出按天计费", "JOB_POST_FREE", 10, true),
                exchangeRow("发薪结算", "全局适用", "fa-money-bill-wave", "#3B82F6",
                        "1 分/元", "无限制", "每笔发薪金额 × 1 积分", "PAYROLL_SETTLE", 20, true),
                exchangeRow("考勤打卡核验", "全局适用", "fa-clipboard-check", "#10B981",
                        "5 分/次", "200 次", "每日核验次数 × 5 积分", "ATTENDANCE_CHECK", 30, true),
                exchangeRow("星级认证专属", "条件触发", "fa-star", "#9333EA",
                        "+20% 加成", "—", "信用分 ≥ 480 自动生效", "STAR_CERT_BONUS", 40, true),
                exchangeRow("招聘专场服务费", "全局适用", "fa-calendar-day", "#F59E0B",
                        "500 分/场", "每月 3 场", "报名专场自动扣费", "JOB_FAIR_SERVICE", 50, true),
                exchangeRow("财务报表导出", "全局适用", "fa-file-invoice", "#3B82F6",
                        "20 分/次", "10 次", "每导出一次扣 20 积分", "REPORT_EXPORT", 60, false)
        ));
    }

    private void seedEarnsIfEmpty() {
        if (earns.count() > 0) return;
        earns.saveAll(List.of(
                earnRow("完成日结任务", "日常任务", "fa-briefcase", "#FF8C5A",
                        "1 分/元", "500 分/天", "完工确认后按工价自动发放", "DAILY_TASK_FINISH", 10, true),
                earnRow("每日签到", "签到任务", "fa-calendar-check", "#10B981",
                        "5 分/天", "每日 1 次", "签到自动到账，连签 7 天额外 +50 分", "DAILY_CHECKIN", 20, true),
                earnRow("邀请好友", "邀请拉新", "fa-user-plus", "#3B82F6",
                        "100 分/人", "每月 20 人", "好友完成首单后到账", "INVITE_FRIEND", 30, true),
                earnRow("实名认证奖励", "一次性任务", "fa-id-card", "#9333EA",
                        "200 分/次", "仅 1 次", "完成实名认证后一次性发放", "REALNAME_BONUS", 40, true),
                earnRow("好评奖励", "互动任务", "fa-thumbs-up", "#F59E0B",
                        "20 分/次", "每日 3 次", "获得老板 5 星评价后发放", "POSITIVE_REVIEW", 50, true),
                earnRow("分享岗位", "分享任务", "fa-share-nodes", "#06B6D4",
                        "10 分/次", "每日 5 次", "好友点击分享链接后到账", "SHARE_JOB", 60, true),
                earnRow("连续接单加成", "条件触发", "fa-fire", "#EF4444",
                        "+10% 加成", "—", "本周完成 ≥ 5 单自动生效", "CONSECUTIVE_ORDERS", 70, false)
        ));
    }

    private static PointPackageSetting packageRow(String name, String sub, Long points, Long price,
                                                  String originalPrice, String save, boolean rec, int sort) {
        PointPackageSetting p = new PointPackageSetting();
        p.setName(name);
        p.setSub(sub);
        p.setPoints(points);
        p.setPrice(BigDecimal.valueOf(price));
        p.setOriginalPrice(originalPrice);
        p.setSave(save);
        p.setRec(rec);
        p.setSort(sort);
        p.setEnabled(true);
        return p;
    }

    private static PointExchangeRule exchangeRow(String name, String sub, String icon, String iconColor,
                                                 String rate, String limit, String cond, String ruleCode,
                                                 int sort, boolean enabled) {
        PointExchangeRule r = new PointExchangeRule();
        r.setName(name);
        r.setSub(sub);
        r.setIcon(icon);
        r.setIconColor(iconColor);
        r.setRate(rate);
        r.setLimit(limit);
        r.setCond(cond);
        r.setRuleCode(ruleCode);
        r.setSort(sort);
        r.setEnabled(enabled);
        return r;
    }

    private static PointEarnRule earnRow(String name, String sub, String icon, String iconColor,
                                         String rate, String limit, String cond, String ruleCode,
                                         int sort, boolean enabled) {
        PointEarnRule r = new PointEarnRule();
        r.setName(name);
        r.setSub(sub);
        r.setIcon(icon);
        r.setIconColor(iconColor);
        r.setRate(rate);
        r.setLimit(limit);
        r.setCond(cond);
        r.setRuleCode(ruleCode);
        r.setSort(sort);
        r.setEnabled(enabled);
        return r;
    }
}

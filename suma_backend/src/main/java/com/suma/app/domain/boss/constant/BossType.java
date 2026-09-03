package com.suma.app.domain.boss.constant;

/**
 * 招工类型常量
 */
public final class BossType {

    private BossType() {
    }

    /** 每天日结 */
    public static final String DAILY = "daily";

    /** 压薪日结 */
    public static final String HELD_BACK = "heldBack";

    /** 月结 */
    public static final String MONTH = "month";

    /** 是否为合法的招工类型 */
    public static boolean isValid(String type) {
        return DAILY.equals(type) || HELD_BACK.equals(type) || MONTH.equals(type);
    }
}

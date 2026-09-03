package com.suma.app.domain.user.constant;

/**
 * 用户角色常量
 */
public final class UserRole {

    private UserRole() {
    }

    /** 老板 */
    public static final String BOSS = "BOSS";

    /** 员工 */
    public static final String USER = "USER";

    /** 是否为合法的角色值 */
    public static boolean isValid(String role) {
        return BOSS.equals(role) || USER.equals(role);
    }
}

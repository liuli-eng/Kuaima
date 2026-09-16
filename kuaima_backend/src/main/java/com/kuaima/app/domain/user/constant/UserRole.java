package com.kuaima.app.domain.user.constant;

import com.kuaima.app.domain.user.entity.User;

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

    /** 是否已通过企业认证；该资格不等同于当前登录 role。 */
    public static boolean hasApprovedEnterprise(User user) {
        return user != null && (CertificationStatus.APPROVED.equals(user.getEnterpriseStatus())
                || ("ENTERPRISE".equalsIgnoreCase(user.getCertType()) && "已通过".equals(user.getCertStatus())));
    }
}

package com.kuaima.app.domain.user.constant;

import com.kuaima.app.domain.user.entity.User;
import org.springframework.util.StringUtils;

/** 企业认证通过后生成的稳定运营编号。 */
public final class EnterpriseCode {
    private EnterpriseCode() {
    }

    public static void ensure(User user) {
        if (user == null || StringUtils.hasText(user.getCompanyCode())) return;
        if (user.getId() == null) throw new IllegalStateException("用户ID为空，无法生成企业编号");
        user.setCompanyCode("ENT" + String.format("%08d", user.getId()));
    }
}

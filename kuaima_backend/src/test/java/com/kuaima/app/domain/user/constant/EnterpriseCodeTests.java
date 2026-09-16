package com.kuaima.app.domain.user.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kuaima.app.domain.user.entity.User;
import org.junit.jupiter.api.Test;

class EnterpriseCodeTests {
    @Test
    void createsStableCodeFromUserId() {
        User user = new User();
        user.setId(43L);
        EnterpriseCode.ensure(user);
        assertEquals("ENT00000043", user.getCompanyCode());

        user.setId(99L);
        EnterpriseCode.ensure(user);
        assertEquals("ENT00000043", user.getCompanyCode());
    }
}

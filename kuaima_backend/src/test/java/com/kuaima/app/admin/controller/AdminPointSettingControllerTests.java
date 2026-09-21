package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.kuaima.app.admin.entity.PointPackageSetting;
import com.kuaima.app.admin.repository.PointEarnRuleRepository;
import com.kuaima.app.admin.repository.PointExchangeRuleRepository;
import com.kuaima.app.admin.repository.PointPackageSettingRepository;
import com.kuaima.app.admin.repository.PointPurchaseOrderRepository;
import com.kuaima.app.security.model.LoginUser;

class AdminPointSettingControllerTests {

    @Test
    void savePackageShouldAllowOnePointAtOneCent() {
        PointPackageSettingRepository packages = mock(PointPackageSettingRepository.class);
        var controller = new AdminPointSettingController(packages, mock(PointExchangeRuleRepository.class),
                mock(PointEarnRuleRepository.class), mock(PointPurchaseOrderRepository.class));
        when(packages.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Map<String, Object> body = new HashMap<>();
        body.put("name", "一分体验包");
        body.put("points", 1);
        body.put("price", 0.01);
        body.put("enabled", true);

        var result = controller.savePackage(body, new UsernamePasswordAuthenticationToken(
                new LoginUser(1L, "admin", "ADMIN_ADMIN"), null));

        assertEquals(1L, result.getData().get("points"));
        assertEquals(new BigDecimal("0.01"), result.getData().get("price"));
    }
}

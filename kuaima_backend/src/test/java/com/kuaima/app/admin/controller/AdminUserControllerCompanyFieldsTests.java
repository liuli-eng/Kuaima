package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

class AdminUserControllerCompanyFieldsTests {
    @Test
    void bossesAlwaysReturnsCompanyFieldsEvenWhenUnverified() {
        UserRepository users = mock(UserRepository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        BossOrderRespository orders = mock(BossOrderRespository.class);
        User user = new User(); user.setId(59L); user.setRole("BOSS");
        when(users.searchBosses(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(user)));
        when(orders.countByCreateByIds(any())).thenReturn(List.of());
        AdminUserController controller = new AdminUserController(users, items, orders);

        var row = controller.bosses(null, null, null, 0, 100).getData().getContent().get(0);

        assertTrue(row.containsKey("companyCode"));
        assertTrue(row.containsKey("companyName"));
        assertNull(row.get("companyCode"));
        assertNull(row.get("companyName"));
    }
}

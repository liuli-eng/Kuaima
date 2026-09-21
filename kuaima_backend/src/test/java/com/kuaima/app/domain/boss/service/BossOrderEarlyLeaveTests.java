package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.*;
import com.kuaima.app.domain.boss.repository.*;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.entity.Settlement;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.kuaima.app.admin.entity.AdminSetting;
import com.kuaima.app.admin.repository.AdminSettingRepository;
import com.kuaima.app.domain.boss.repository.BossRecruitSettingsRepository;
import com.kuaima.app.domain.coupon.service.BossCouponService;
import com.kuaima.app.domain.user.service.CertificationService;

class BossOrderEarlyLeaveTests {
    @Test
    void earlyLeaveMovesOrderToPendingSettlementAndCreatesSettlement() {
        BossOrderRespository orders = mock(BossOrderRespository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        BossOrderService service = new BossOrderService(orders, items, mock(UserRepository.class),
                mock(MessageService.class), settlements);
        BossOrder order = new BossOrder(); order.setId(45L); order.setSalary(200);
        order.setOrderStatus(BossStatus.ORDER_RECRUIT_END);
        BaseOrderItem item = new BaseOrderItem(); item.setId(70L); item.setOrderId(45L); item.setUserId(30L);
        item.setStatus(BossStatus.ITEM_ON_WORK); item.setWorkDate(Date.valueOf(LocalDate.now()));
        when(items.findById(70L)).thenReturn(Optional.of(item));
        when(orders.findById(45L)).thenReturn(Optional.of(order));
        when(orders.save(order)).thenReturn(order);
        when(items.findByOrderId(45L)).thenReturn(List.of(item));
        when(settlements.existsByItemIdAndStatusIn(eq(70L), any())).thenReturn(false);

        BossOrder result = service.finishByEarlyLeave(70L);

        assertEquals(BossStatus.ITEM_PENDING_SETTLE, item.getStatus());
        assertTrue(item.getEarlyLeave());
        assertEquals(BossStatus.ORDER_PENDING_SETTLE, result.getOrderStatus());
        verify(settlements).save(argThat(s -> s.getItemId().equals(70L)
                && s.getOrderId().equals(45L) && s.getWorkerId().equals(30L)
                && "待支付".equals(s.getStatus())));
    }

    @Test
    void autoCreatedSettlementUsesConfiguredPlatformFee() {
        BossOrderRespository orders = mock(BossOrderRespository.class);
        BaseOrderItemRespository items = mock(BaseOrderItemRespository.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        AdminSettingRepository settings = mock(AdminSettingRepository.class);
        BossOrderService service = new BossOrderService(orders, items, mock(UserRepository.class),
                mock(MessageService.class), settlements, mock(CertificationService.class),
                mock(BossRecruitSettingsRepository.class), mock(BossCouponService.class), settings);
        BossOrder order = new BossOrder(); order.setId(46L); order.setSalary(200);
        order.setOrderStatus(BossStatus.ORDER_RECRUIT_END);
        BaseOrderItem item = new BaseOrderItem(); item.setId(71L); item.setOrderId(46L); item.setUserId(31L);
        item.setStatus(BossStatus.ITEM_ON_WORK); item.setWorkDate(Date.valueOf(LocalDate.now()));
        when(items.findById(71L)).thenReturn(Optional.of(item));
        when(orders.findById(46L)).thenReturn(Optional.of(order));
        when(orders.save(order)).thenReturn(order);
        when(items.findByOrderId(46L)).thenReturn(List.of(item));
        when(settlements.existsByItemIdAndStatusIn(eq(71L), any())).thenReturn(false);
        when(settings.findById("rules.platformFeeEnabled")).thenReturn(Optional.of(setting("true")));
        when(settings.findById("rules.feeRate")).thenReturn(Optional.of(setting("5")));

        service.finishByEarlyLeave(71L);

        verify(settlements).save(argThat(s -> new java.math.BigDecimal("200").equals(s.getWage())
                && new java.math.BigDecimal("10.00").equals(s.getServiceFee())
                && new java.math.BigDecimal("210.00").equals(s.getTotalAmount())));
    }

    private AdminSetting setting(String value) {
        AdminSetting setting = new AdminSetting();
        setting.setSettingValue(value);
        return setting;
    }
}

package com.kuaima.app.domain.reward.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSONObject;
import com.kuaima.app.domain.reward.entity.RewardRechargeOrder;
import com.kuaima.app.domain.reward.repository.RewardRechargeOrderRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.wechat.service.WechatPayService;

class RewardRechargeServiceTests {
    private RewardRechargeOrderRepository orders;
    private UserRepository users;
    private WechatPayService wechatPay;
    private RewardLedgerService ledger;
    private RewardRechargeService service;

    @BeforeEach
    void setUp() {
        orders = mock(RewardRechargeOrderRepository.class);
        users = mock(UserRepository.class);
        wechatPay = mock(WechatPayService.class);
        ledger = mock(RewardLedgerService.class);
        service = new RewardRechargeService(orders, users, wechatPay, ledger);
        User boss = new User();
        boss.setId(7L);
        boss.setEnterpriseStatus("APPROVED");
        boss.setOpenid("openid-7");
        when(users.findById(7L)).thenReturn(Optional.of(boss));
        when(orders.saveAndFlush(any())).thenAnswer(invocation -> {
            RewardRechargeOrder order = invocation.getArgument(0);
            order.setId(81L);
            return order;
        });
        when(wechatPay.prepay(any(), any(), any(), any())).thenReturn(new JSONObject());
    }

    @Test
    void createsWechatPrepayWithYuanAmount() {
        var result = service.create(7L, new BigDecimal("0.02"), "request-1");

        assertEquals(new BigDecimal("0.02"), result.get("amount"));
        assertEquals("PENDING", result.get("status"));
        verify(wechatPay).prepay(any(), any(), org.mockito.ArgumentMatchers.eq(new BigDecimal("0.02")),
                org.mockito.ArgumentMatchers.eq("openid-7"));
    }

    @Test
    void duplicateKeyCannotChangeAmount() {
        RewardRechargeOrder old = order();
        when(orders.findByIdempotencyKey("reward-recharge:7:request-1")).thenReturn(Optional.of(old));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(7L, new BigDecimal("2.00"), "request-1"));
    }

    @Test
    void callbackCreditsRewardOnlyOnce() {
        RewardRechargeOrder order = order();
        when(orders.findByOrderNoForUpdate("RR202609220001")).thenReturn(Optional.of(order));
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.markPaidByWechatCallback("RR202609220001", "wx-transaction-1");
        service.markPaidByWechatCallback("RR202609220001", "wx-transaction-1");

        assertEquals("PAID", order.getStatus());
        assertEquals(true, order.getRewardCredited());
        verify(ledger, times(1)).credit(7L, new BigDecimal("0.01"), "REWARD_RECHARGE", 81L,
                "微信充值奖励金", "微信支付奖励金充值", "REWARD_RECHARGE:81");
    }

    private RewardRechargeOrder order() {
        RewardRechargeOrder order = new RewardRechargeOrder();
        order.setId(81L);
        order.setOrderNo("RR202609220001");
        order.setIdempotencyKey("reward-recharge:7:request-1");
        order.setUserId(7L);
        order.setAmount(new BigDecimal("0.01"));
        order.setBonusAmount(BigDecimal.ZERO.setScale(2));
        order.setPayMethod("WECHAT");
        order.setStatus("PENDING");
        order.setRewardCredited(false);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}

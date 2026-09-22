package com.kuaima.app.domain.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.boss.entity.BossBalanceRechargeOrder;
import com.kuaima.app.domain.boss.entity.BossMerchantAccount;
import com.kuaima.app.domain.boss.repository.BossBalanceRechargeOrderRepository;
import com.kuaima.app.domain.boss.repository.BossMerchantAccountRepository;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.wechat.service.WechatPayService;

class BossBalancePaymentServiceTests {
    @Test
    void duplicateWechatCallbackCreditsBossWalletOnce() {
        BossBalanceRechargeOrderRepository orders = mock(BossBalanceRechargeOrderRepository.class);
        BossMerchantAccountRepository accounts = mock(BossMerchantAccountRepository.class);
        BossBalanceRechargeOrder order = new BossBalanceRechargeOrder(); order.setOrderNo("RB1");
        order.setBossId(7L); order.setAmount(new BigDecimal("0.02")); order.setStatus("pending");
        BossMerchantAccount account = new BossMerchantAccount(); account.setId(9L); account.setBossId(7L); account.setBalance(100L);
        when(orders.findByOrderNoForUpdate("RB1")).thenReturn(Optional.of(order));
        when(accounts.findFirstByBossIdAndIsDefaultTrue(7L)).thenReturn(Optional.of(account));
        when(orders.save(any())).thenAnswer(i -> i.getArgument(0));
        BossBalancePaymentService service = new BossBalancePaymentService(orders, accounts,
                mock(UserRepository.class), mock(WechatPayService.class));

        service.markPaid("RB1", "wx1");
        service.markPaid("RB1", "wx1");

        assertEquals(102L, account.getBalance());
        verify(accounts, times(1)).save(account);
    }
}

package com.kuaima.app.domain.reward.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.reward.entity.RewardWithdrawal;
import com.kuaima.app.domain.reward.repository.RewardAccountRepository;
import com.kuaima.app.domain.reward.repository.RewardFlowRepository;
import com.kuaima.app.domain.reward.repository.RewardWithdrawalRepository;
import com.kuaima.app.domain.reward.service.RewardWithdrawSettingsService.RewardWithdrawSettings;
import com.kuaima.app.domain.reward.service.WechatMerchantTransferClient.MerchantTransferResult;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;

class BossRewardWithdrawalTests {
    @Test
    void bossWithdrawalUsesWechatTransferWithoutDeductingWalletAgain() {
        UserRepository users = mock(UserRepository.class);
        RewardWithdrawSettingsService settings = mock(RewardWithdrawSettingsService.class);
        WorkerRewardWithdrawalProcessor processor = mock(WorkerRewardWithdrawalProcessor.class);
        WechatMerchantTransferClient transfer = mock(WechatMerchantTransferClient.class);
        User boss = new User(); boss.setId(7L); boss.setEnterpriseStatus("APPROVED"); boss.setOpenid("openid-7");
        when(users.findById(7L)).thenReturn(Optional.of(boss));
        RewardWithdrawSettings config = new RewardWithdrawSettings(1000L, true, null, "WECHAT");
        when(settings.settings()).thenReturn(config);
        RewardWithdrawal withdrawal = withdrawal();
        when(processor.existing(eq(7L), eq(1000L), eq("WECHAT"), any())).thenReturn(Optional.empty());
        when(processor.submit(eq(7L), eq(1000L), eq("WECHAT"), eq(config), any())).thenReturn(withdrawal);
        MerchantTransferResult accepted = new MerchantTransferResult("RWB1", "batch-1", null, "ACCEPTED", "{}");
        when(transfer.transfer(any())).thenReturn(accepted);
        when(processor.transferAccepted(withdrawal, accepted)).thenReturn(withdrawal);
        BossRewardService service = new BossRewardService(mock(RewardAccountRepository.class),
                mock(RewardFlowRepository.class), mock(RewardWithdrawalRepository.class), users,
                settings, processor, transfer);

        var result = service.withdraw(7L, new BigDecimal("10.00"), "request-1");

        assertEquals(new BigDecimal("10.00"), result.get("amount"));
        verify(transfer).transfer(any(WechatMerchantTransferClient.MerchantTransferRequest.class));
        verify(processor).submit(7L, 1000L, "WECHAT", config, "boss-reward:7:request-1");
    }

    private RewardWithdrawal withdrawal() {
        RewardWithdrawal withdrawal = new RewardWithdrawal();
        withdrawal.setId(91L); withdrawal.setUserId(7L); withdrawal.setAmount(new BigDecimal("10.00"));
        withdrawal.setChannel("WECHAT"); withdrawal.setStatus("PENDING"); withdrawal.setMerchantBatchNo("RWB1");
        withdrawal.setMerchantDetailNo("RWD1"); withdrawal.setAppliedAt(LocalDateTime.now());
        return withdrawal;
    }
}

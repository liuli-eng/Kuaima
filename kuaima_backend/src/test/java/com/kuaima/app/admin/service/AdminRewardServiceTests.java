package com.kuaima.app.admin.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.kuaima.app.admin.entity.RewardCampaign;
import com.kuaima.app.admin.repository.*;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.admin.entity.RewardFundAccount;
import com.kuaima.app.domain.wallet.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminRewardServiceTests {
    private RewardCampaignRepository campaigns;
    private RewardGrantRepository grants;
    private RewardFundAccountRepository funds;
    private RewardFundFlowRepository fundFlows;
    private UserRepository users;
    private WalletRespository wallets;
    private WalletFlowRespository walletFlows;
    private AdminRewardService service;

    @BeforeEach
    void setUp() {
        campaigns = mock(RewardCampaignRepository.class);
        grants = mock(RewardGrantRepository.class);
        funds = mock(RewardFundAccountRepository.class);
        fundFlows = mock(RewardFundFlowRepository.class);
        users = mock(UserRepository.class);
        wallets = mock(WalletRespository.class);
        walletFlows = mock(WalletFlowRespository.class);
        service = new AdminRewardService(campaigns, grants, funds, fundFlows, users, wallets, walletFlows, null);
    }

    @Test
    void rejectsInvalidTargetBeforeWriting() {
        Map<String, Object> body = Map.of("name", "活动", "target", "未知", "scope", "全部",
                "amountMode", "fixed", "sendMode", "now", "count", 1, "amount", 10);
        assertThrows(IllegalArgumentException.class, () -> service.create(body, 1L, "管理员"));
        verifyNoInteractions(campaigns, users);
    }

    @Test
    void rejectsScheduledTimeInPast() {
        Map<String, Object> body = Map.of("name", "活动", "target", "老板", "scope", "全部",
                "amountMode", "fixed", "sendMode", "timing", "sendAt", "2020-01-01 00:00:00", "count", 1, "amount", 10);
        User boss = new User(); boss.setId(7L); boss.setRole("BOSS");
        when(users.findAll()).thenReturn(java.util.List.of(boss));
        assertThrows(IllegalArgumentException.class, () -> service.create(body, 1L, "管理员"));
        verify(campaigns, never()).save(any());
    }

    @Test
    void insufficientBudgetDoesNotCreditWallet() {
        RewardCampaign campaign = new RewardCampaign();
        campaign.setId(2L); campaign.setStatus("待发放"); campaign.setUsers("[7]");
        campaign.setAmountMode("fixed"); campaign.setAmount(1000L);
        RewardFundAccount account = new RewardFundAccount(); account.setBalance(999L);
        when(campaigns.findById(2L)).thenReturn(Optional.of(campaign));
        when(grants.existsByCampaignIdAndUserId(2L, 7L)).thenReturn(false);
        when(funds.findForUpdate(1L)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class, () -> service.execute(2L));
        verifyNoInteractions(wallets, walletFlows, fundFlows);
        verify(campaigns, never()).save(any());
    }

    @Test
    void missingCampaignIsNotFound() {
        when(campaigns.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(99L));
    }

    @Test
    void alreadyDistributedCampaignIsIdempotent() {
        RewardCampaign campaign = new RewardCampaign();
        campaign.setId(1L); campaign.setStatus("已发放");
        when(campaigns.findById(1L)).thenReturn(Optional.of(campaign));
        assertEquals(campaign, service.execute(1L));
        verifyNoInteractions(grants, funds, wallets, walletFlows, fundFlows);
    }

    @Test
    void distributedCampaignCannotBeCanceled() {
        RewardCampaign campaign = new RewardCampaign();
        campaign.setId(3L); campaign.setStatus("已发放");
        when(campaigns.findById(3L)).thenReturn(Optional.of(campaign));
        assertThrows(IllegalArgumentException.class, () -> service.cancel(3L));
        verify(campaigns, never()).save(any());
    }
}

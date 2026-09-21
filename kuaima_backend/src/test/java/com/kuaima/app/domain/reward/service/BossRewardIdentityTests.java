package com.kuaima.app.domain.reward.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import com.kuaima.app.domain.reward.entity.RewardAccount;
import com.kuaima.app.domain.reward.repository.*;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.*;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BossRewardIdentityTests {
    @Test
    void certifiedBossCanReadRewardAfterSwitchingLoginRoleToUser() {
        RewardAccountRepository accounts = mock(RewardAccountRepository.class);
        RewardFlowRepository flows = mock(RewardFlowRepository.class);
        UserRepository users = mock(UserRepository.class);
        User user = new User(); user.setId(7L); user.setRole("USER"); user.setCertType("ENTERPRISE"); user.setCertStatus("已通过");
        RewardAccount account = new RewardAccount(); account.setUserId(7L); account.setBalance(new BigDecimal("20.00"));
        when(users.findById(7L)).thenReturn(Optional.of(user)); when(accounts.findByUserId(7L)).thenReturn(Optional.of(account));
        when(flows.sumByUserIdAndType(7L, "INCOME")).thenReturn(new BigDecimal("20.00")); when(flows.sumByUserIdAndType(7L, "EXPENSE")).thenReturn(BigDecimal.ZERO);
        BossRewardService service = new BossRewardService(accounts, flows, mock(RewardWithdrawalRepository.class),
                mock(RewardLedgerService.class), mock(WalletRespository.class), mock(WalletFlowRespository.class), users);

        var result = service.overview(7L);

        assertEquals(new BigDecimal("20.00"), result.get("balance"));
    }
}

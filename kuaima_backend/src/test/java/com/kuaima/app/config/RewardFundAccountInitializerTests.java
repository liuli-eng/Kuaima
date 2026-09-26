package com.kuaima.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.kuaima.app.admin.entity.RewardFundAccount;
import com.kuaima.app.admin.repository.RewardFundAccountRepository;

class RewardFundAccountInitializerTests {

    @Test
    void createsEmptyPlatformBudgetAccountWhenMissing() {
        RewardFundAccountRepository repository = mock(RewardFundAccountRepository.class);
        when(repository.existsById(1L)).thenReturn(false);

        new RewardFundAccountInitializer(repository).run();

        verify(repository).save(argThat(account -> account.getId().equals(1L)
                && BigDecimal.ZERO.compareTo(account.getBalance()) == 0));
    }

    @Test
    void doesNotOverwriteExistingBudget() {
        RewardFundAccountRepository repository = mock(RewardFundAccountRepository.class);
        when(repository.existsById(1L)).thenReturn(true);

        new RewardFundAccountInitializer(repository).run();

        verify(repository, never()).save(any());
    }
}

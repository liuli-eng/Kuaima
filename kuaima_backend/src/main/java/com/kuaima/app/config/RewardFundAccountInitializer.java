package com.kuaima.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.kuaima.app.admin.entity.RewardFundAccount;
import com.kuaima.app.admin.repository.RewardFundAccountRepository;

/** 确保平台奖励预算账户存在；仅创建账户，不向预算充值。 */
@Component
public class RewardFundAccountInitializer implements CommandLineRunner {

    private final RewardFundAccountRepository accounts;

    public RewardFundAccountInitializer(RewardFundAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public void run(String... args) {
        if (accounts.existsById(1L)) return;
        RewardFundAccount account = new RewardFundAccount();
        account.setId(1L);
        account.setBalance(java.math.BigDecimal.ZERO);
        accounts.save(account);
    }
}

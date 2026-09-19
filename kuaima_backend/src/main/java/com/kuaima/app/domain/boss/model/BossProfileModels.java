package com.kuaima.app.domain.boss.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAlias;

public final class BossProfileModels {
    private BossProfileModels() {}

    public record ProfileStats(int totalOrders, long recruitingCount, long applicantCount,
                               BigDecimal settledAmount, int integrityScore, int goodRate,
                               int arrivalRate, int settleRate, BigDecimal totalPayment,
                               long completedOrders) {}

    public record RecruitAccounts(boolean hasAuthorizedEmployee, long authorizedEmployeeCount,
                                  Long currentAccountId, java.util.List<AccountView> accounts) {}

    public record SwitchAccountRequest(Long accountId) {}

    public record AccountView(Long id, String name, String avatar, String phone,
                              String authorizationType, String workCode, String leaveCode,
                              boolean current) {}

    public record AddAccountRequest(String phone,
                                   @JsonAlias({"code", "smsCode"}) String smsCode,
                                   @JsonAlias({"agreed", "agreementAccepted"}) Boolean agreementAccepted) {}

    public record AddedAccountView(Long id, String name, String avatar, String phone,
                                   String authorizationType, boolean current,
                                   boolean newlyRegistered) {}

    public record AddAccountResponse(AddedAccountView account, Long currentAccountId) {}

    public record QuickLoginResponse(String accessToken, Long userId, String username, String role,
                                     String phone, String certStatus) {}
}

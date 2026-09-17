package com.kuaima.app.domain.boss.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public final class BossProfileModels {
    private BossProfileModels() {}

    public record ProfileStats(int totalOrders, long recruitingCount, long applicantCount,
                               double settledAmount, int integrityScore, int goodRate,
                               int arrivalRate, int settleRate, long totalPayment,
                               long completedOrders) {}

    public record RecruitAccounts(boolean hasAuthorizedEmployee, long authorizedEmployeeCount,
                                  Long currentAccountId, java.util.List<AccountView> accounts) {}

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
}

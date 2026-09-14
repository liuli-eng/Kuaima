package com.kuaima.app.domain.boss.model;

public final class BossProfileModels {
    private BossProfileModels() {}

    public record ProfileStats(int totalOrders, long recruitingCount, long applicantCount,
                               double settledAmount, int integrityScore, int goodRate,
                               int arrivalRate, int settleRate, long totalPayment,
                               long completedOrders) {}

    public record RecruitAccounts(boolean hasAuthorizedEmployee, long authorizedEmployeeCount,
                                  Long currentAccountId, java.util.List<AccountView> accounts) {}

    public record AccountView(Long id, String name, String avatar, String authorizationType,
                              String workCode, String leaveCode, boolean current) {}
}

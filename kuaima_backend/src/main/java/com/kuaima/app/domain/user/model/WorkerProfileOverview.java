package com.kuaima.app.domain.user.model;

/** 零工个人页 JWT 聚合数据；金额单位均为分，比例范围为 0-100。 */
public record WorkerProfileOverview(
        int level,
        int creditScore,
        int completionRate,
        int cancellationRate,
        int noShowRate,
        int earlyLeaveRate,
        long totalIncome,
        long completedOrders,
        int points,
        long rewardAmount) {
}

package com.kuaima.app.domain.boss.model;

import java.time.LocalDate;
import java.util.List;

public final class BossHomeModels {
    private BossHomeModels() {}

    public record Account(Long id, String name, String avatar, String authorizationType,
                          String workCode, String leaveCode) {}

    public record ScheduleDay(LocalDate date, String label, int demand) {}

    public record Overview(String city, long nearbyWorkers, int fastestMinutes,
                           Long currentAccountId, Account account,
                           List<ScheduleDay> scheduleDays) {}

    public record ScheduleStats(long accepted, long arrived, long working,
                                long finished, long settled) {}

    public record ScheduleRecord(Long orderId, String title, String sub, String status,
                                 String statusCode, String jobType) {}

    public record Schedule(LocalDate date, int demand, ScheduleStats stats,
                           List<ScheduleRecord> records) {}

    public record BossStats(int totalOrders, long recruitingCount, long applicantCount,
                            double settledAmount, long nearbyWorkers,
                            int fastestMinutes, String city) {}
}

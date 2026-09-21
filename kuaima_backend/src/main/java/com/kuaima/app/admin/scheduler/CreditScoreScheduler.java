package com.kuaima.app.admin.scheduler;

import com.kuaima.app.admin.service.ScheduledJobRunner;
import com.kuaima.app.domain.user.service.CreditScorePeriodicService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditScoreScheduler {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final CreditScorePeriodicService service;
    private final ScheduledJobRunner jobs;

    @Value("${jobs.credit.effective-from:2026-09-19T00:00:00}")
    private LocalDateTime effectiveFrom;

    @Scheduled(cron = "${jobs.credit.daily-cron:0 0 2 * * ?}", zone = "Asia/Shanghai")
    public void daily() {
        daily("AUTO");
    }

    public void daily(String triggerType) {
        jobs.run("credit-score-daily", LocalDate.now(ZONE).toString(), triggerType,
                () -> {
                    LocalDateTime now = LocalDateTime.now(ZONE);
                    service.processOverdueSettlements(effectiveFrom, now);
                });
    }

    @Scheduled(cron = "${jobs.credit.monthly-cron:0 0 3 1 * ?}", zone = "Asia/Shanghai")
    public void monthly() {
        monthly("AUTO");
    }

    public void monthly(String triggerType) {
        YearMonth month = YearMonth.now(ZONE).minusMonths(1);
        jobs.run("credit-score-monthly", month.toString(), triggerType,
                () -> service.processWorkerMonthlyCompletion(month));
    }
}

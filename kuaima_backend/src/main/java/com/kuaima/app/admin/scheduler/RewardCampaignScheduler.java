package com.kuaima.app.admin.scheduler;
import java.time.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.kuaima.app.admin.repository.RewardCampaignRepository;
import com.kuaima.app.admin.service.AdminRewardService;
import com.kuaima.app.admin.service.ScheduledJobRunner;

@Component
public class RewardCampaignScheduler {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private final RewardCampaignRepository repo;
    private final AdminRewardService service;
    private final ScheduledJobRunner jobs;
    public RewardCampaignScheduler(RewardCampaignRepository r, AdminRewardService s, ScheduledJobRunner j) {
        repo = r; service = s; jobs = j;
    }
    @Scheduled(fixedDelay = 60000)
    public void executeDue() {
        executeDue("AUTO");
    }
    public void executeDue(String triggerType) {
        jobs.run("reward-campaign", LocalDateTime.now(ZONE).toLocalDate().toString(), triggerType, () -> {
            for (var c : repo.findByStatusAndSendAtLessThanEqual("待发放", LocalDateTime.now(ZONE))) {
                try { service.execute(c.getId()); }
                catch (Exception e) { service.recordFailure(c.getId(), e.getMessage()); }
            }
        });
    }
}

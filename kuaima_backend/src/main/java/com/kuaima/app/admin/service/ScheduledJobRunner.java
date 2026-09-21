package com.kuaima.app.admin.service;

import com.kuaima.app.admin.entity.ScheduledJobExecution;
import com.kuaima.app.admin.repository.ScheduledJobExecutionRepository;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobRunner {
    private final JdbcTemplate jdbc;
    private final ScheduledJobExecutionRepository executions;

    public void run(String jobCode, String periodKey, Runnable task) {
        run(jobCode, periodKey, "AUTO", task);
    }

    public void run(String jobCode, String periodKey, String triggerType, Runnable task) {
        String lockName = "kuaima:job:" + jobCode;
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            try (var acquire = connection.prepareStatement("SELECT GET_LOCK(?, 0)")) {
                acquire.setString(1, lockName);
                try (var result = acquire.executeQuery()) {
                    if (!result.next() || !result.getBoolean(1)) {
                        log.info("定时任务已有其他实例执行，跳过 jobCode={}", jobCode);
                        return null;
                    }
                }
            }
            ScheduledJobExecution execution = new ScheduledJobExecution();
            execution.setJobCode(jobCode);
            execution.setPeriodKey(periodKey);
            execution.setStatus("RUNNING");
            execution.setStartedAt(LocalDateTime.now());
            execution.setTriggerType(triggerType);
            execution.setInstanceId(instanceId());
            execution = executions.save(execution);
            try {
                task.run();
                execution.setStatus("SUCCESS");
            } catch (RuntimeException e) {
                execution.setStatus("FAILED");
                execution.setErrorMessage(truncate(e.getMessage()));
                throw e;
            } finally {
                execution.setFinishedAt(LocalDateTime.now());
                executions.save(execution);
                try (var release = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
                    release.setString(1, lockName);
                    release.executeQuery().close();
                }
            }
            return null;
        });
    }

    private String instanceId() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return UUID.randomUUID().toString();
        }
    }

    private String truncate(String message) {
        if (message == null || message.length() <= 2000) return message;
        return message.substring(0, 2000);
    }
}

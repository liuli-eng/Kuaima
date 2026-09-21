package com.kuaima.app.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "scheduled_job_execution")
@Getter
@Setter
public class ScheduledJobExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String jobCode;
    @Column(length = 80)
    private String periodKey;
    @Column(nullable = false, length = 20)
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    @Column(length = 2000)
    private String errorMessage;
    @Column(length = 100)
    private String instanceId;
    @Column(nullable = false, length = 20)
    private String triggerType;
}

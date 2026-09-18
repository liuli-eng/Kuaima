package com.kuaima.app.domain.boss.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name = "boss_attendance_code", uniqueConstraints = @UniqueConstraint(name = "uk_boss_attendance_code_day", columnNames = {"enterprise_id", "code_date"}))
@Getter @Setter
public class BossAttendanceCode extends BaseEntity {
    @Column(name = "enterprise_id") private Long enterpriseId;
    @Column(name = "boss_id", nullable = false) private Long bossId;
    @Column(name = "code_date", nullable = false) private LocalDate codeDate;
    private String workCode;
    private String leaveCode;
    private Boolean workCodeEnabled;
    private Boolean leaveCodeEnabled;
    private LocalDateTime workCodeExpiresAt;
    private LocalDateTime leaveCodeExpiresAt;
    private Integer workFailedAttempts;
    private Integer leaveFailedAttempts;
    private LocalDateTime workLockedUntil;
    private LocalDateTime leaveLockedUntil;
}

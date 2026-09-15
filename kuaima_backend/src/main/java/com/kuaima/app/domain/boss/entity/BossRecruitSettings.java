package com.kuaima.app.domain.boss.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "boss_recruit_settings", uniqueConstraints = @UniqueConstraint(name = "uk_boss_recruit_settings_owner", columnNames = "boss_id"))
@Getter @Setter
public class BossRecruitSettings extends BaseEntity {
    @Column(name = "boss_id", nullable = false) private Long bossId;
    private String phoneMode;
    private String backupPhone;
    private String stopTime;
    private Boolean dndEnabled;
    private Boolean startCodeEnabled;
    private Boolean earlyCodeEnabled;
    private String attendanceMode;
    private Boolean faceClockEnabled;
    private String settleMode;
    private String type;
    private String signMode;
    private Boolean phoneNotify;
    private Boolean signNotify;
    private Boolean startRemind;
    private Boolean settleNotify;
}

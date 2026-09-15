package com.kuaima.app.domain.boss.model;

public final class BossRecruitSettingsModels {
    private BossRecruitSettingsModels() {}
    public record Settings(String phoneMode, String backupPhone, String stopTime, Boolean dndEnabled,
            Boolean startCodeEnabled, Boolean earlyCodeEnabled, String attendanceMode, Boolean faceClockEnabled,
            String settleMode, String type, String signMode, Boolean phoneNotify, Boolean signNotify,
            Boolean startRemind, Boolean settleNotify) {}
}

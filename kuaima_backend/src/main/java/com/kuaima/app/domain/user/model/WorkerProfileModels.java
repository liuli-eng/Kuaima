package com.kuaima.app.domain.user.model;

import java.time.LocalDate;

public final class WorkerProfileModels {

    public record WorkerProfile(
            Long id,
            String avatar,
            String nickname,
            String phone,
            String gender,
            LocalDate birthday,
            String city,
            String skills,
            Integer workYears,
            Boolean acceptNightShift,
            String introduction) {
    }

    public record UpdateWorkerProfileRequest(
            String avatar,
            String nickname,
            String gender,
            LocalDate birthday,
            String city,
            String skills,
            Integer workYears,
            Boolean acceptNightShift,
            String introduction) {
    }

    private WorkerProfileModels() {
    }
}

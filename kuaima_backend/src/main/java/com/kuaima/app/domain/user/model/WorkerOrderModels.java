package com.kuaima.app.domain.user.model;

import java.util.Date;

public final class WorkerOrderModels {

    public record WorkerOrder(
            Long id,
            Long orderId,
            String status,
            Boolean trialRequested,
            Date applyDate,
            Date hireDate,
            Date workDate,
            Date finishDate,
            String orderTitle,
            String type,
            Integer salary,
            String address,
            Date startTime,
            Date endTime,
            String companyName,
            String postion,
            String orderStatus,
            Integer duration,
            String tags) {
    }

    private WorkerOrderModels() {
    }
}

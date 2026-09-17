package com.kuaima.app.domain.user.model;

import java.util.Date;
import com.kuaima.app.domain.review.model.BossReviewModels.ReviewSummary;

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
            String tags,
            boolean reviewed,
            ReviewSummary bossReview) {
    }

    private WorkerOrderModels() {
    }
}

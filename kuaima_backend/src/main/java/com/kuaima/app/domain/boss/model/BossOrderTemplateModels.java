package com.kuaima.app.domain.boss.model;

import java.math.BigDecimal;

import java.util.Date;

public final class BossOrderTemplateModels {
    private BossOrderTemplateModels() {}

    public record TemplateView(Long id, String templateName, Long sourceOrderId,
                               String orderTitle, String orderContent, String type,
                               Long industryId, java.util.List<Long> enterpriseTypeIds, java.util.List<Long> jobIds,
                               Long jobCategoryId, String postion, String address,
                               BigDecimal longitude, BigDecimal latitude,
                               BigDecimal salary, Integer duration, Integer orderNum,
                               Date startTime, Date endTime, String tags,
                               String experience, String gender, String signMode,
                               Boolean phoneNotify, Boolean signNotify, Boolean startRemind, Boolean settleNotify,
                               java.util.List<Long> invitedWorkerIds) {}

    public record CreateRequest(String templateName, Boolean overwrite) {}
    public record RenameRequest(String templateName) {}
}

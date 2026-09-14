package com.kuaima.app.domain.boss.model;

import java.util.Date;

public final class BossOrderTemplateModels {
    private BossOrderTemplateModels() {}

    public record TemplateView(Long id, String templateName, Long sourceOrderId,
                               String orderTitle, String type, String postion,
                               Integer salary, Integer duration, Integer orderNum,
                               Date startTime, Date endTime, String tags,
                               String experience, String gender) {}

    public record CreateRequest(String templateName, Boolean overwrite) {}
    public record RenameRequest(String templateName) {}
}

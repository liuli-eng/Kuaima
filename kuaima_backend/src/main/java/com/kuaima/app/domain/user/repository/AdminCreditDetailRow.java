package com.kuaima.app.domain.user.repository;

import java.time.LocalDateTime;

/** 后台信用详情单次 JOIN 查询投影。 */
public interface AdminCreditDetailRow {
    Long getUserId();
    String getBusinessId();
    Integer getCreditScore();
    Integer getStarScore();
    String getScoreType();
    Long getFlowId();
    String getFlowBizNo();
    Integer getDelta();
    Integer getBeforeScore();
    Integer getAfterScore();
    String getRuleCode();
    String getBizType();
    String getReason();
    /** Native MySQL timestamp projections are exposed by Hibernate as LocalDateTime. */
    LocalDateTime getFlowTimestamp();
}

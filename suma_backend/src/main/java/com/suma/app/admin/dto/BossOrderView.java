package com.suma.app.admin.dto;

/**
 * BossOrder 列表视图：在实体基础上扩展 employerName（雇主企业名/昵称），供 admin 管理后台展示。
 */
public record BossOrderView(
        Long id,
        String orderTitle,
        String orderContent,
        String orderNo,
        Integer orderNum,
        String orderStatus,
        String type,
        String postion,
        Integer duration,
        Integer salary,
        String address,
        String tags,
        String trialDuration,
        java.sql.Timestamp timestamp,
        java.util.Date startTime,
        java.util.Date endTime,
        String employerName
) {
}

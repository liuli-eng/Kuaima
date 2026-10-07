package com.kuaima.app.domain.coupon.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/** 后台老板优惠券详情的单次 JOIN 查询投影。 */
public interface AdminCouponRecordRow {
    Long getId();
    String getStatus();
    Date getExpireAt();
    Timestamp getUsedAt();
    Long getUseOrderId();
    String getTitle();
    String getType();
    BigDecimal getAmount();
    BigDecimal getMinSpend();
    BigDecimal getDiscount();
    BigDecimal getCap();
    Long getTotalCount();
}

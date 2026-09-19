package com.kuaima.app.domain.payroll.entity;

import java.math.BigDecimal;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 发薪明细（发薪单下的人员明细）。金额以「元」存储。 */
@Entity
@Table(name = "payroll_detail", indexes = {
        @Index(name = "idx_pd_payroll", columnList = "payroll_id"),
        @Index(name = "idx_pd_status", columnList = "status")
})
@Setter
@Getter
public class PayrollDetail extends BaseEntity {
    public void setDailyWage(Long v) { this.dailyWage = v == null ? null : BigDecimal.valueOf(v); }
    public void setDailyWage(long v) { this.dailyWage = BigDecimal.valueOf(v); }
    public void setDailyWage(BigDecimal v) { this.dailyWage = v; }
    public void setAmount(Long v) { this.amount = v == null ? null : BigDecimal.valueOf(v); }
    public void setAmount(long v) { this.amount = BigDecimal.valueOf(v); }
    public void setAmount(BigDecimal v) { this.amount = v; }

    @Column(comment = "关联 payroll_order.id")
    private Long payrollId;

    @Column(comment = "姓名")
    private String name;

    @Column(comment = "手机号")
    private String phone;

    @Column(comment = "岗位")
    private String job;

    @Column(comment = "出勤天数")
    private Integer attendDays = 0;

    @Column(precision = 18, scale = 2, comment = "日薪（元）")
    private BigDecimal dailyWage = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2, comment = "应发金额（元）")
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(comment = "状态:pending待转移/success转账成功/failed转账失败")
    private String status = "pending";

    @Column(comment = "转账单号，如 TR20260910001")
    private String orderNo;

    @Column(comment = "支付账户，如 招商银行 · ****6688")
    private String account;

    @Column(comment = "支付时间")
    private java.util.Date payTime;
}

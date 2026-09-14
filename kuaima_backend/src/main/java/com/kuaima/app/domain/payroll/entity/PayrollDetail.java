package com.kuaima.app.domain.payroll.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 发薪明细（发薪单下的人员明细）。金额以「分」存储。 */
@Entity
@Table(name = "payroll_detail", indexes = {
        @Index(name = "idx_pd_payroll", columnList = "payroll_id"),
        @Index(name = "idx_pd_status", columnList = "status")
})
@Setter
@Getter
public class PayrollDetail extends BaseEntity {

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

    @Column(comment = "日薪（分）")
    private Long dailyWage = 0L;

    @Column(comment = "应发金额（分）")
    private Long amount = 0L;

    @Column(comment = "状态:pending待转账/success转账成功/failed转账失败")
    private String status = "pending";
}

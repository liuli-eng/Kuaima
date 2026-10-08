package com.kuaima.app.domain.payroll.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 发薪明细（发薪单下的人员明细）。金额以「分」存储，与 payroll_detail BIGINT 列一致。 */
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

    @Column(name = "user_id", comment = "收款人用户 id（project_member.user_id 优先，回退手机号匹配 sys_user）")
    private Long userId;

    @Column(comment = "姓名")
    private String name;

    @Column(comment = "手机号")
    private String phone;

    @Column(comment = "岗位")
    private String job;

    @Column(comment = "出勤天数")
    private Integer attendDays;

    @Column(columnDefinition = "BIGINT", comment = "日薪（分）")
    private Long dailyWage;

    @Column(columnDefinition = "BIGINT", comment = "应发金额（分）")
    private Long amount;

    @Column(comment = "状态:pending待转移/success转账成功/failed转账失败")
    private String status = "pending";

    @Column(comment = "转账单号，如 TR20260910001")
    private String orderNo;

    @Column(comment = "支付账户，如 招商银行 · ****6688")
    private String account;

    @Column(comment = "支付时间")
    private java.util.Date payTime;

    @Column(length = 500, comment = "备注/失败原因，如 无收款账号、非本项目成员")
    private String remark;
}

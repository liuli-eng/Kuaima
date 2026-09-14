package com.kuaima.app.domain.payroll.entity;

import java.util.Date;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 发薪单（后台发薪管理核心实体）。金额以「分」存储。 */
@Entity
@Table(name = "payroll_order", indexes = {
        @Index(name = "idx_payroll_project", columnList = "project_id"),
        @Index(name = "idx_payroll_status", columnList = "status"),
        @Index(name = "idx_payroll_submit", columnList = "submit_time")
})
@Setter
@Getter
public class PayrollOrder extends BaseEntity {

    @Column(comment = "发薪单号，如 TR20260910001")
    private String orderNo;

    @Column(comment = "所属公司")
    private String company;

    @Column(nullable = false, comment = "薪单标题")
    private String title;

    @Column(comment = "关联 project.id")
    private Long projectId;

    @Column(comment = "所属项目")
    private String projectName;

    @Column(comment = "转账类型:wage工资/advance预支/other其他")
    private String type = "wage";

    @Column(comment = "应发总金额（分）")
    private Long amount = 0L;

    @Column(comment = "发薪人数")
    private Integer peopleCount = 0;

    @Column(comment = "制单人员")
    private String creator;

    @Column(comment = "制单人员 id")
    private Long creatorId;

    @Column(comment = "提交时间")
    private Date submitTime;

    @Column(comment = "状态:pending待审批/approved审批通过/rejected已驳回/withdrawn已撤回")
    private String status = "pending";

    @Column(comment = "审核人员")
    private String reviewBy;

    @Column(comment = "审核时间")
    private Date reviewTime;
}

package com.kuaima.app.domain.project.entity;

import java.time.LocalDate;
import java.util.Date;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 入职申请记录。 */
@Entity
@Table(name = "onboard_apply", indexes = {
        @Index(name = "idx_ob_project", columnList = "project_id"),
        @Index(name = "idx_ob_status", columnList = "`status`")
})
@Setter
@Getter
public class OnboardApply extends BaseEntity {

    @Column(name = "project_id", comment = "关联 project.id")
    private Long projectId;

    @Column(comment = "申请人姓名")
    private String name;

    @Column(comment = "手机号")
    private String phone;

    @Column(comment = "申请职位")
    private String job;

    @Column(name = "apply_role", comment = "申请角色，如 项目负责人")
    private String applyRole;

    @Column(name = "intent_project", comment = "意向项目")
    private String intentProject;

    @Column(name = "apply_time", comment = "申请时间")
    private Date applyTime;

    @Column(name = "`status`", comment = "状态:pending审核中/passed已通过/rejected已拒绝")
    private String status = "pending";

    @Column(name = "join_date", comment = "入职日期")
    private LocalDate joinDate;

    @Column(name = "salary_expect", comment = "期望薪资")
    private String salaryExpect;

    @Column(name = "work_time", comment = "工时类型，如 全职/兼职")
    private String workTime;

    @Column(comment = "所属公司")
    private String company;

    @Column(name = "reviewer", comment = "审核人")
    private String reviewer;

    @Column(name = "review_time", comment = "审核时间")
    private Date reviewTime;
}

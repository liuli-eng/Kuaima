package com.kuaima.app.domain.payroll.entity;

import java.time.LocalDate;
import java.util.Date;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 老板端发薪员工（uniapp 发薪模块-员工Tab）。金额以「分」存储。 */
@Entity
@Table(name = "payroll_employee", indexes = {
        @Index(name = "idx_pe_boss", columnList = "boss_id"),
        @Index(name = "idx_pe_status", columnList = "status"),
        @Index(name = "idx_pe_phone", columnList = "phone")
})
@Setter
@Getter
public class PayrollEmployee extends BaseEntity {

    @Column(name = "boss_id", nullable = false, columnDefinition = "BIGINT COMMENT '老板用户 id'")
    private Long bossId;

    @Column(name = "`name`", nullable = false, columnDefinition = "VARCHAR(50) COMMENT '姓名'")
    private String name;

    @Column(columnDefinition = "VARCHAR(10) COMMENT '性别:男/女'")
    private String gender;

    @Column(columnDefinition = "INT COMMENT '年龄'")
    private Integer age;

    @Column(columnDefinition = "VARCHAR(20) COMMENT '手机号'")
    private String phone;

    @Column(name = "id_card", columnDefinition = "VARCHAR(30) COMMENT '身份证号（脱敏存储）'")
    private String idCard;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT '是否已实名认证'")
    private Boolean certified;

    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'active' COMMENT '状态:active在职/temp临时/left离职'")
    private String status;

    @Column(name = "project_id", columnDefinition = "BIGINT COMMENT '关联 project.id'")
    private Long projectId;

    @Column(name = "project_name", columnDefinition = "VARCHAR(120) COMMENT '所属项目名称'")
    private String projectName;

    @Column(columnDefinition = "VARCHAR(50) COMMENT '岗位'")
    private String position;

    @Column(name = "employment_type", columnDefinition = "VARCHAR(20) COMMENT '用工类型:全职/兼职/临时'")
    private String employmentType;

    @Column(name = "daily_wage", columnDefinition = "BIGINT COMMENT '日薪标准（分）'")
    private Long dailyWage;

    @Column(name = "join_date", columnDefinition = "DATE COMMENT '入职时间'")
    private LocalDate joinDate;

    @Column(name = "add_time", columnDefinition = "DATETIME COMMENT '添加时间'")
    private Date addTime;

    @Column(name = "total_attend_days", nullable = false, columnDefinition = "INT DEFAULT 0 COMMENT '累计出勤天数'")
    private Integer totalAttendDays;

    @Column(name = "total_paid", nullable = false, columnDefinition = "BIGINT DEFAULT 0 COMMENT '累计发薪（分）'")
    private Long totalPaid;

    @Column(name = "work_days", nullable = false, columnDefinition = "INT DEFAULT 0 COMMENT '在岗天数'")
    private Integer workDays;
}

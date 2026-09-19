package com.kuaima.app.domain.position.entity;

import java.time.LocalDateTime;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "boss_position", indexes = {
        @Index(name = "idx_position_status", columnList = "status"),
        @Index(name = "idx_position_category", columnList = "category"),
        @Index(name = "idx_position_department", columnList = "department"),
        @Index(name = "idx_position_create_by", columnList = "create_by")
})
@Setter
@Getter
public class BossPosition extends BaseEntity {

    @Column(name = "`name`", nullable = false, columnDefinition = "VARCHAR(100) COMMENT '岗位名称'")
    private String name;

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(50) COMMENT '岗位编码'")
    private String code;

    @Column(columnDefinition = "VARCHAR(50) COMMENT '岗位类别'")
    private String category;

    @Column(columnDefinition = "VARCHAR(50) COMMENT '所属部门'")
    private String department;

    @Column(columnDefinition = "VARCHAR(200) COMMENT '工作地点'")
    private String location;

    @Column(name = "hire_count", nullable = false, columnDefinition = "INT DEFAULT 0 COMMENT '招聘人数'")
    private Integer hireCount;

    @Column(name = "salary_range", columnDefinition = "VARCHAR(50) COMMENT '薪资范围'")
    private String salaryRange;

    @Column(columnDefinition = "VARCHAR(30) COMMENT '工作经验要求'")
    private String experience;

    @Column(columnDefinition = "VARCHAR(30) COMMENT '学历要求'")
    private String education;

    @Column(columnDefinition = "TEXT COMMENT '岗位描述'")
    private String description;

    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'on' COMMENT '状态:on在招中/off已停用'")
    private String status;

    @Column(name = "apply_count", nullable = false, columnDefinition = "INT DEFAULT 0 COMMENT '已投递人数'")
    private Integer applyCount;

    @Column(name = "interview_count", nullable = false, columnDefinition = "INT DEFAULT 0 COMMENT '面试中人数'")
    private Integer interviewCount;

    @Column(name = "hired_count", nullable = false, columnDefinition = "INT DEFAULT 0 COMMENT '已录用人数'")
    private Integer hiredCount;

    @Column(name = "hot_count", nullable = false, columnDefinition = "INT DEFAULT 0 COMMENT '热度计数(投递)'")
    private Integer hotCount;

    @Column(name = "publish_time", columnDefinition = "DATETIME COMMENT '发布时间'")
    private LocalDateTime publishTime;

}

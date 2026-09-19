package com.kuaima.app.domain.resume.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 简历经历：教育经历(EDU)/工作经历(WORK)/项目经验(PROJECT)。 */
@Entity
@Table(name = "boss_resume_experience", indexes = {
        @Index(name = "idx_resume_exp_resume", columnList = "resume_id,type")
})
@Getter
@Setter
public class ResumeExperience extends BaseEntity {

    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    /** EDU / WORK / PROJECT */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 100)
    private String org;

    @Column(name = "start_date", length = 20)
    private String startDate;

    @Column(name = "end_date", length = 20)
    private String endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 亮点标签，逗号分隔，如：分拣打包,扫码录入 */
    @Column(length = 200)
    private String tags;
}

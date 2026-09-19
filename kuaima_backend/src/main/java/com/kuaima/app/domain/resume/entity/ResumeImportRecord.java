package com.kuaima.app.domain.resume.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 简历导入记录：上传的简历文件及解析结果。 */
@Entity
@Table(name = "boss_resume_import", indexes = {
        @Index(name = "idx_resume_import_boss", columnList = "boss_id")
})
@Getter
@Setter
public class ResumeImportRecord extends BaseEntity {

    @Column(name = "boss_id", nullable = false)
    private Long bossId;

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(nullable = false, length = 200)
    private String fileName;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /** SUCCESS / FAILED */
    @Column(nullable = false, length = 20)
    private String status = "SUCCESS";

    @Column(name = "resume_id")
    private Long resumeId;

    @Column(length = 200)
    private String failReason;
}

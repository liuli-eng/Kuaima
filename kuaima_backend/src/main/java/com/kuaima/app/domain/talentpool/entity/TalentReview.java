package com.kuaima.app.domain.talentpool.entity;

import java.time.LocalDate;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "talent_review", indexes = {
        @Index(name = "idx_tr_talent", columnList = "talent_id")
})
@Setter
@Getter
public class TalentReview extends BaseEntity {

    @Column(name = "talent_id", nullable = false, columnDefinition = "BIGINT COMMENT '关联 talent_pool.id'")
    private Long talentId;

    @Column(name = "boss_id", columnDefinition = "BIGINT COMMENT '评价老板用户 id'")
    private Long bossId;

    @Column(name = "job_title", columnDefinition = "VARCHAR(100) COMMENT '工作岗位标题'")
    private String jobTitle;

    @Column(columnDefinition = "VARCHAR(100) COMMENT '工作地点'")
    private String location;

    @Column(name = "review_date", columnDefinition = "DATE COMMENT '评价日期'")
    private LocalDate reviewDate;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 5 COMMENT '星级 1-5'")
    private Integer stars;

    @Column(columnDefinition = "VARCHAR(500) COMMENT '评价内容'")
    private String content;
}

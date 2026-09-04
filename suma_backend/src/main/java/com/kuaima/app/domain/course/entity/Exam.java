package com.kuaima.app.domain.course.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 课程小测：及格分 passScore，关联多道 ExamQuestion。
 */
@Entity
@Table(name = "course_exam")
@Getter
@Setter
public class Exam extends BaseEntity {

    @Column(comment = "所属课程ID")
    private Long courseId;

    @Column(length = 100, comment = "试卷标题")
    private String title;

    @Column(comment = "及格分")
    private Integer passScore;
}

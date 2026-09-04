package com.kuaima.app.domain.course.entity;

import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 考试成绩：单次作答得分与是否通过，takenAt 记录作答时间。
 */
@Entity
@Table(name = "course_exam_result")
@Getter
@Setter
public class ExamResult extends BaseEntity {

    @Column(comment = "考生用户ID")
    private Long userId;

    @Column(comment = "试卷ID")
    private Long examId;

    @Column(comment = "得分")
    private Integer score;

    @Column(comment = "是否通过")
    private Boolean passed;

    @Column(comment = "作答时间")
    private Timestamp takenAt;
}

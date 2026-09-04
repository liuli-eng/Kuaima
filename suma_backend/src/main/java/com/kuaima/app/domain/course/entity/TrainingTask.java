package com.kuaima.app.domain.course.entity;

import java.sql.Date;
import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 培训任务：分配给某用户的某课程，状态流转 PENDING->COMPLETED。
 */
@Entity
@Table(name = "course_training_task")
@Getter
@Setter
public class TrainingTask extends BaseEntity {

    @Column(comment = "受训用户ID")
    private Long userId;

    @Column(comment = "课程ID")
    private Long courseId;

    @Column(length = 20, comment = "状态: PENDING/COMPLETED")
    private String status;

    @Column(comment = "截止日期")
    private Date dueDate;

    @Column(comment = "完成时间")
    private Timestamp completedAt;
}

package com.kuaima.app.domain.course.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 试题：options 以分隔符存储多选项（如 A|B|C|D），answer 存正确选项。
 */
@Entity
@Table(name = "course_exam_question")
@Getter
@Setter
public class ExamQuestion extends BaseEntity {

    @Column(comment = "所属试卷ID")
    private Long examId;

    @Column(length = 500, comment = "题干")
    private String content;

    @Column(length = 500, comment = "选项(分隔符存储)")
    private String options;

    @Column(length = 50, comment = "正确答案")
    private String answer;

    @Column(comment = "题目分值")
    private Integer score;
}

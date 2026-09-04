package com.kuaima.app.domain.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.course.entity.ExamQuestion;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    /** 某试卷所有题目 */
    List<ExamQuestion> findByExamId(Long examId);
}

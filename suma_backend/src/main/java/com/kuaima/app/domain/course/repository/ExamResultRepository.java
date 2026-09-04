package com.kuaima.app.domain.course.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.course.entity.ExamResult;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    /** 查询某用户某试卷的最新成绩 */
    Optional<ExamResult> findFirstByUserIdAndExamIdOrderByIdDesc(Long userId, Long examId);
}

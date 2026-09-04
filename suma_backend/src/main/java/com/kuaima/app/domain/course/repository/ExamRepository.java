package com.kuaima.app.domain.course.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.course.entity.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    /** 按课程查询单条试卷 */
    Optional<Exam> findByCourseId(Long courseId);
}

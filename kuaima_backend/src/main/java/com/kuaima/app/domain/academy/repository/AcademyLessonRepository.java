package com.kuaima.app.domain.academy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.academy.entity.AcademyLesson;

public interface AcademyLessonRepository extends JpaRepository<AcademyLesson, Long> {
    List<AcademyLesson> findAllByOrderByIdAsc();

    Optional<AcademyLesson> findByLessonKey(String lessonKey);
}

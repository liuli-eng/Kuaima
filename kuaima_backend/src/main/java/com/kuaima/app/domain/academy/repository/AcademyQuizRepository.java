package com.kuaima.app.domain.academy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.academy.entity.AcademyQuiz;

public interface AcademyQuizRepository extends JpaRepository<AcademyQuiz, Long> {
    List<AcademyQuiz> findByTypeOrderBySortAscIdAsc(String type);

    List<AcademyQuiz> findAllByOrderBySortAscIdAsc();
}

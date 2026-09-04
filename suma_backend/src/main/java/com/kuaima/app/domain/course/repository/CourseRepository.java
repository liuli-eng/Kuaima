package com.kuaima.app.domain.course.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.course.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    /** 按分类分页查询（category 为空时全量） */
    Page<Course> findByCategory(String category, Pageable pageable);
}

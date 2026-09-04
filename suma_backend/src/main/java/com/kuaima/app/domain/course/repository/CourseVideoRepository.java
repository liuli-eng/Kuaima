package com.kuaima.app.domain.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.course.entity.CourseVideo;

public interface CourseVideoRepository extends JpaRepository<CourseVideo, Long> {

    /** 某课程全部视频，按 sortOrder 正序 */
    List<CourseVideo> findByCourseIdOrderBySortOrderAsc(Long courseId);
}

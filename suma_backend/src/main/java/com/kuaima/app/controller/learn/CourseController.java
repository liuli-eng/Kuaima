package com.kuaima.app.controller.learn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.course.entity.Course;
import com.kuaima.app.domain.course.entity.CourseVideo;
import com.kuaima.app.domain.course.repository.CourseRepository;
import com.kuaima.app.domain.course.repository.CourseVideoRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 培训课程。
 */
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final CourseVideoRepository videoRepository;

    public CourseController(CourseRepository courseRepository, CourseVideoRepository videoRepository) {
        this.courseRepository = courseRepository;
        this.videoRepository = videoRepository;
    }

    /** 课程大厅分页列表：GET /courses?category=&page=0&size=20 */
    @GetMapping
    public Result<List<Course>> listCourses(@RequestParam(required = false) String category,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Course> result;
        if (category != null && !category.isEmpty()) {
            result = courseRepository.findByCategory(category, pageable);
        } else {
            result = courseRepository.findAll(pageable);
        }
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    /** 课程详情（含视频列表）：GET /courses/{id} */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getCourse(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("课程不存在: " + id));
        List<CourseVideo> videos = videoRepository.findByCourseIdOrderBySortOrderAsc(id);
        Map<String, Object> data = new HashMap<>();
        data.put("course", course);
        data.put("videos", videos);
        return Result.success(data);
    }

    /** 课程视频列表：GET /courses/{id}/videos */
    @GetMapping("/{id}/videos")
    public Result<List<CourseVideo>> listVideos(@PathVariable Long id) {
        return Result.success(videoRepository.findByCourseIdOrderBySortOrderAsc(id));
    }
}

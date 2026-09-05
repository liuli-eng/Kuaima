package com.kuaima.app.domain.course.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.course.entity.TrainingTask;

public interface TrainingTaskRepository extends JpaRepository<TrainingTask, Long> {

    /** 某用户全部培训任务 */
    List<TrainingTask> findByUserId(Long userId);
}

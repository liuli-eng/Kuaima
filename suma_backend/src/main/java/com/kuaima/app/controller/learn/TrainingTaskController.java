package com.kuaima.app.controller.learn;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.course.entity.TrainingTask;
import com.kuaima.app.domain.course.repository.TrainingTaskRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 培训任务。
 */
@RestController
@RequestMapping("/training-tasks")
public class TrainingTaskController {

    private final TrainingTaskRepository taskRepository;

    public TrainingTaskController(TrainingTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /** 某用户培训任务列表：GET /training-tasks?userId=1 */
    @GetMapping
    public Result<List<TrainingTask>> listTasks(@RequestParam Long userId) {
        return Result.success(taskRepository.findByUserId(userId));
    }

    /** 分配培训任务：POST /training-tasks  body: { "userId": 1, "courseId": 2 } */
    @PostMapping
    @Transactional
    public Result<TrainingTask> assignTask(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
        Long courseId = body.get("courseId") != null ? Long.valueOf(body.get("courseId").toString()) : null;
        if (userId == null || courseId == null) {
            throw new IllegalArgumentException("userId 和 courseId 不能为空");
        }
        TrainingTask task = new TrainingTask();
        task.setUserId(userId);
        task.setCourseId(courseId);
        task.setStatus("PENDING");
        return Result.success(taskRepository.save(task));
    }

    /** 完成培训任务：PUT /training-tasks/{id}/complete */
    @PutMapping("/{id}/complete")
    @Transactional
    public Result<TrainingTask> completeTask(@PathVariable Long id) {
        TrainingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("培训任务不存在: " + id));
        task.setStatus("COMPLETED");
        task.setCompletedAt(new Timestamp(System.currentTimeMillis()));
        return Result.success(taskRepository.save(task));
    }
}

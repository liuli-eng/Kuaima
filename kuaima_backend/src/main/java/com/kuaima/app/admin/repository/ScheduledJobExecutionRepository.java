package com.kuaima.app.admin.repository;

import com.kuaima.app.admin.entity.ScheduledJobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScheduledJobExecutionRepository extends JpaRepository<ScheduledJobExecution, Long> {
    Page<ScheduledJobExecution> findAllByOrderByStartedAtDesc(Pageable pageable);
    Page<ScheduledJobExecution> findByJobCodeOrderByStartedAtDesc(String jobCode, Pageable pageable);
}

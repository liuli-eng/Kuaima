package com.kuaima.app.domain.jobcategory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.jobcategory.entity.JobCategoryHot;

public interface JobCategoryHotRepository extends JpaRepository<JobCategoryHot, Long> {

    List<JobCategoryHot> findByEnabledTrueOrderBySortNoAscIdAsc();
}

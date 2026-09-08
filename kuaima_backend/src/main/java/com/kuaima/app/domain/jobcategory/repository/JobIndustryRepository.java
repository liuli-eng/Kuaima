package com.kuaima.app.domain.jobcategory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.jobcategory.entity.JobIndustry;

public interface JobIndustryRepository extends JpaRepository<JobIndustry, Long> {

    List<JobIndustry> findByEnabledTrueOrderBySortNoAscIdAsc();
}

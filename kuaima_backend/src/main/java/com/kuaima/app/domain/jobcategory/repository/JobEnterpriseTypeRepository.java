package com.kuaima.app.domain.jobcategory.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.jobcategory.entity.JobEnterpriseType;

public interface JobEnterpriseTypeRepository extends JpaRepository<JobEnterpriseType, Long> {

    boolean existsByIndustryIdAndNameIgnoreCase(Long industryId, String name);

    @org.springframework.data.jpa.repository.Query("select coalesce(max(e.sortNo), 0) from JobEnterpriseType e where e.industryId = :industryId")
    Integer findMaxSortNoByIndustryId(@org.springframework.data.repository.query.Param("industryId") Long industryId);

    List<JobEnterpriseType> findByIndustryIdInAndEnabledTrueOrderBySortNoAscIdAsc(Collection<Long> industryIds);

    List<JobEnterpriseType> findByIndustryIdAndEnabledTrueOrderBySortNoAscIdAsc(Long industryId);
}

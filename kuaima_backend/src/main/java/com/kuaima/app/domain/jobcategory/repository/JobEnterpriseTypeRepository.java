package com.kuaima.app.domain.jobcategory.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.jobcategory.entity.JobEnterpriseType;

public interface JobEnterpriseTypeRepository extends JpaRepository<JobEnterpriseType, Long> {

    List<JobEnterpriseType> findByIndustryIdInAndEnabledTrueOrderBySortNoAscIdAsc(Collection<Long> industryIds);

    List<JobEnterpriseType> findByIndustryIdAndEnabledTrueOrderBySortNoAscIdAsc(Long industryId);
}

package com.kuaima.app.domain.jobcategory.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.jobcategory.entity.JobCategory;

public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {

    boolean existsByIndustryIdAndEnterpriseTypeIdAndNameIgnoreCase(
            Long industryId, Long enterpriseTypeId, String name);

    @Query("select coalesce(max(j.sortNo), 0) from JobCategory j where j.enterpriseTypeId = :enterpriseTypeId")
    Integer findMaxSortNoByEnterpriseTypeId(@Param("enterpriseTypeId") Long enterpriseTypeId);

    List<JobCategory> findByEnterpriseTypeIdInAndEnabledTrueOrderBySortNoAscIdAsc(Collection<Long> enterpriseTypeIds);

    List<JobCategory> findByEnterpriseTypeIdAndEnabledTrueOrderBySortNoAscIdAsc(Long enterpriseTypeId);

    @Query("""
            select j from JobCategory j
            where j.enabled = true
              and (lower(j.name) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(j.description, '')) like lower(concat('%', :keyword, '%')))
            order by j.sortNo asc, j.id asc
            """)
    List<JobCategory> searchEnabled(@Param("keyword") String keyword, Pageable pageable);
}

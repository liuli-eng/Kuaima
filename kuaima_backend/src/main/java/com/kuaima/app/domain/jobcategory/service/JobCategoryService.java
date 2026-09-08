package com.kuaima.app.domain.jobcategory.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.jobcategory.entity.JobCategory;
import com.kuaima.app.domain.jobcategory.entity.JobEnterpriseType;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.EnterpriseItem;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.HotItem;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.IndustryItem;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.JobItem;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.SearchItem;
import com.kuaima.app.domain.jobcategory.repository.JobCategoryHotRepository;
import com.kuaima.app.domain.jobcategory.repository.JobCategoryRepository;
import com.kuaima.app.domain.jobcategory.repository.JobEnterpriseTypeRepository;
import com.kuaima.app.domain.jobcategory.repository.JobIndustryRepository;

@Service
public class JobCategoryService {

    private final JobIndustryRepository industryRepository;
    private final JobEnterpriseTypeRepository enterpriseRepository;
    private final JobCategoryRepository categoryRepository;
    private final JobCategoryHotRepository hotRepository;

    public JobCategoryService(JobIndustryRepository industryRepository,
                              JobEnterpriseTypeRepository enterpriseRepository,
                              JobCategoryRepository categoryRepository,
                              JobCategoryHotRepository hotRepository) {
        this.industryRepository = industryRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.categoryRepository = categoryRepository;
        this.hotRepository = hotRepository;
    }

    /** 一次查询完整的行业、企业类型和工种树，供三级选择页面使用。 */
    public List<IndustryItem> tree() {
        var industries = industryRepository.findByEnabledTrueOrderBySortNoAscIdAsc();
        if (industries.isEmpty()) {
            return List.of();
        }
        var industryIds = industries.stream().map(item -> item.getId()).toList();
        var enterprises = enterpriseRepository
                .findByIndustryIdInAndEnabledTrueOrderBySortNoAscIdAsc(industryIds);
        var enterpriseIds = enterprises.stream().map(item -> item.getId()).toList();
        var jobs = enterpriseIds.isEmpty()
                ? List.<JobCategory>of()
                : categoryRepository.findByEnterpriseTypeIdInAndEnabledTrueOrderBySortNoAscIdAsc(enterpriseIds);

        Map<Long, List<JobCategory>> jobsByEnterprise = jobs.stream()
                .collect(Collectors.groupingBy(JobCategory::getEnterpriseTypeId));
        Map<Long, List<JobEnterpriseType>> enterprisesByIndustry = enterprises.stream()
                .collect(Collectors.groupingBy(JobEnterpriseType::getIndustryId));

        return industries.stream().map(industry -> new IndustryItem(
                industry.getId(),
                industry.getCode(),
                industry.getName(),
                industry.getSortNo(),
                enterprisesByIndustry.getOrDefault(industry.getId(), Collections.emptyList()).stream()
                        .map(enterprise -> new EnterpriseItem(
                                enterprise.getId(),
                                enterprise.getName(),
                                enterprise.getSortNo(),
                                jobsByEnterprise.getOrDefault(enterprise.getId(), Collections.emptyList()).stream()
                                        .map(this::toJobItem)
                                        .toList()))
                        .toList()))
                .toList();
    }

    public List<HotItem> hot() {
        return hotRepository.findByEnabledTrueOrderBySortNoAscIdAsc().stream()
                .map(item -> new HotItem(
                        item.getId(), item.getDisplayName(), item.getDisplayName(),
                        item.getJobCategoryId(), item.getSortNo(), true))
                .toList();
    }

    public List<SearchItem> search(String keyword, int size) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        int safeSize = Math.min(Math.max(size, 1), 100);
        var jobs = categoryRepository.searchEnabled(keyword.trim(), PageRequest.of(0, safeSize));
        if (jobs.isEmpty()) {
            return List.of();
        }
        var enterpriseIds = jobs.stream().map(JobCategory::getEnterpriseTypeId).distinct().toList();
        var enterprises = enterpriseRepository.findAllById(enterpriseIds);
        Map<Long, JobEnterpriseType> enterpriseById = enterprises.stream()
                .collect(Collectors.toMap(JobEnterpriseType::getId, item -> item));
        var industryIds = jobs.stream().map(JobCategory::getIndustryId).distinct().toList();
        Map<Long, String> industryNameById = industryRepository.findAllById(industryIds).stream()
                .collect(Collectors.toMap(item -> item.getId(), item -> item.getName()));

        return jobs.stream()
                .map(item -> {
                    var enterprise = enterpriseById.get(item.getEnterpriseTypeId());
                    return new SearchItem(
                            item.getId(),
                            item.getIndustryId(),
                            industryNameById.get(item.getIndustryId()),
                            item.getEnterpriseTypeId(),
                            enterprise != null ? enterprise.getName() : null,
                            item.getName(),
                            item.getDescription(),
                            item.getSortNo());
                })
                .toList();
    }

    private JobItem toJobItem(JobCategory item) {
        return new JobItem(item.getId(), item.getIndustryId(), item.getEnterpriseTypeId(),
                item.getName(), item.getDescription(), item.getSortNo());
    }
}

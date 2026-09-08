package com.kuaima.app.domain.jobcategory.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.jobcategory.entity.JobCategory;
import com.kuaima.app.domain.jobcategory.entity.JobEnterpriseType;
import com.kuaima.app.domain.jobcategory.entity.JobIndustry;
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

    /** 老板端新增企业类型；分类数据为全局共享数据，不按老板隔离。 */
    @org.springframework.transaction.annotation.Transactional
    public JobEnterpriseType createEnterpriseType(Long industryId, String name, Integer sortNo) {
        String normalizedName = requireName(name, "企业类型名称");
        JobIndustry industry = industryRepository.findById(industryId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("行业不存在: " + industryId));
        if (!Boolean.TRUE.equals(industry.getEnabled())) {
            throw new IllegalArgumentException("行业已停用，不能添加企业类型");
        }
        if (enterpriseRepository.existsByIndustryIdAndNameIgnoreCase(industryId, normalizedName)) {
            throw new IllegalArgumentException("该行业下企业类型已存在: " + normalizedName);
        }
        JobEnterpriseType item = new JobEnterpriseType();
        item.setIndustryId(industryId);
        item.setName(normalizedName);
        item.setSortNo(resolveSortNo(sortNo, enterpriseRepository.findMaxSortNoByIndustryId(industryId)));
        item.setEnabled(true);
        try {
            return enterpriseRepository.saveAndFlush(item);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("该行业下企业类型已存在: " + normalizedName);
        }
    }

    /** 老板端新增工种；工种必须归属于请求中的行业和企业类型。 */
    @org.springframework.transaction.annotation.Transactional
    public JobCategory createJob(Long industryId, Long enterpriseTypeId, String name,
                                 String description, Integer sortNo) {
        String normalizedName = requireName(name, "工种名称");
        JobIndustry industry = industryRepository.findById(industryId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("行业不存在: " + industryId));
        if (!Boolean.TRUE.equals(industry.getEnabled())) {
            throw new IllegalArgumentException("行业已停用，不能添加工种");
        }
        JobEnterpriseType enterprise = enterpriseRepository.findById(enterpriseTypeId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("企业类型不存在: " + enterpriseTypeId));
        if (!industryId.equals(enterprise.getIndustryId())) {
            throw new IllegalArgumentException("企业类型不属于指定行业");
        }
        if (!Boolean.TRUE.equals(enterprise.getEnabled())) {
            throw new IllegalArgumentException("企业类型已停用，不能添加工种");
        }
        if (categoryRepository.existsByIndustryIdAndEnterpriseTypeIdAndNameIgnoreCase(
                industryId, enterpriseTypeId, normalizedName)) {
            throw new IllegalArgumentException("该企业类型下工种已存在: " + normalizedName);
        }
        JobCategory item = new JobCategory();
        item.setIndustryId(industryId);
        item.setEnterpriseTypeId(enterpriseTypeId);
        item.setName(normalizedName);
        String normalizedDescription = StringUtils.hasText(description) ? description.trim() : null;
        if (normalizedDescription != null && normalizedDescription.length() > 500) {
            throw new IllegalArgumentException("工种描述长度不能超过500个字符");
        }
        item.setDescription(normalizedDescription);
        item.setSortNo(resolveSortNo(sortNo, categoryRepository.findMaxSortNoByEnterpriseTypeId(enterpriseTypeId)));
        item.setEnabled(true);
        try {
            return categoryRepository.saveAndFlush(item);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("该企业类型下工种已存在: " + normalizedName);
        }
    }

    private String requireName(String name, String field) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException(field + "不能为空");
        }
        String normalized = name.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException(field + "长度不能超过100个字符");
        }
        return normalized;
    }

    private int resolveSortNo(Integer requested, Integer currentMax) {
        if (requested != null && requested > 0) {
            return requested;
        }
        return (currentMax == null ? 0 : currentMax) + 1;
    }

    private JobItem toJobItem(JobCategory item) {
        return new JobItem(item.getId(), item.getIndustryId(), item.getEnterpriseTypeId(),
                item.getName(), item.getDescription(), item.getSortNo());
    }
}

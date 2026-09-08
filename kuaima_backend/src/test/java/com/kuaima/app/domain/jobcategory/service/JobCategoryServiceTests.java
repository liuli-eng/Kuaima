package com.kuaima.app.domain.jobcategory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.jobcategory.entity.JobCategory;
import com.kuaima.app.domain.jobcategory.entity.JobCategoryHot;
import com.kuaima.app.domain.jobcategory.entity.JobEnterpriseType;
import com.kuaima.app.domain.jobcategory.entity.JobIndustry;
import com.kuaima.app.domain.jobcategory.repository.JobCategoryHotRepository;
import com.kuaima.app.domain.jobcategory.repository.JobCategoryRepository;
import com.kuaima.app.domain.jobcategory.repository.JobEnterpriseTypeRepository;
import com.kuaima.app.domain.jobcategory.repository.JobIndustryRepository;

class JobCategoryServiceTests {

    private JobIndustryRepository industryRepository;
    private JobEnterpriseTypeRepository enterpriseRepository;
    private JobCategoryRepository categoryRepository;
    private JobCategoryHotRepository hotRepository;
    private JobCategoryService service;

    @BeforeEach
    void setUp() {
        industryRepository = mock(JobIndustryRepository.class);
        enterpriseRepository = mock(JobEnterpriseTypeRepository.class);
        categoryRepository = mock(JobCategoryRepository.class);
        hotRepository = mock(JobCategoryHotRepository.class);
        service = new JobCategoryService(
                industryRepository, enterpriseRepository, categoryRepository, hotRepository);
    }

    @Test
    void tree_shouldAssembleIndustryEnterpriseAndJobs() {
        JobIndustry industry = industry(1L, "INDUSTRY_001", "制造业工厂", 1);
        JobEnterpriseType enterprise = enterprise(11L, 1L, "电子厂", 1);
        JobCategory job = job(111L, 1L, 11L, "普工", "操作工/流水线", 1);
        when(industryRepository.findByEnabledTrueOrderBySortNoAscIdAsc()).thenReturn(List.of(industry));
        when(enterpriseRepository.findByIndustryIdInAndEnabledTrueOrderBySortNoAscIdAsc(List.of(1L)))
                .thenReturn(List.of(enterprise));
        when(categoryRepository.findByEnterpriseTypeIdInAndEnabledTrueOrderBySortNoAscIdAsc(List.of(11L)))
                .thenReturn(List.of(job));

        var result = service.tree();

        assertEquals(1, result.size());
        assertEquals("制造业工厂", result.get(0).name());
        assertEquals("电子厂", result.get(0).enterpriseTypes().get(0).name());
        assertEquals("普工", result.get(0).enterpriseTypes().get(0).jobs().get(0).name());
        assertEquals("操作工/流水线", result.get(0).enterpriseTypes().get(0).jobs().get(0).description());
    }

    @Test
    void hot_shouldExposeNameForExistingFrontendCompatibility() {
        JobCategoryHot hot = new JobCategoryHot();
        hot.setId(1L);
        hot.setDisplayName("电子厂普工");
        hot.setJobCategoryId(111L);
        hot.setSortNo(1);
        when(hotRepository.findByEnabledTrueOrderBySortNoAscIdAsc()).thenReturn(List.of(hot));

        var result = service.hot();

        assertEquals("电子厂普工", result.get(0).name());
        assertEquals("电子厂普工", result.get(0).displayName());
        assertTrue(result.get(0).hot());
    }

    @Test
    void search_shouldTrimKeywordAndLimitSizeToOneHundred() {
        when(categoryRepository.searchEnabled(any(), any())).thenReturn(List.of());

        service.search("  普工  ", 999);

        verify(categoryRepository).searchEnabled(
                org.mockito.ArgumentMatchers.eq("普工"),
                org.mockito.ArgumentMatchers.argThat(page -> page.getPageSize() == 100));
    }

    @Test
    void search_shouldReturnIndustryAndEnterpriseContextForDuplicateJobNames() {
        JobCategory job = job(111L, 1L, 11L, "普工", "操作工/流水线", 1);
        JobEnterpriseType enterprise = enterprise(11L, 1L, "电子厂", 1);
        JobIndustry industry = industry(1L, "INDUSTRY_001", "制造业工厂", 1);
        when(categoryRepository.searchEnabled(any(), any())).thenReturn(List.of(job));
        when(enterpriseRepository.findAllById(List.of(11L))).thenReturn(List.of(enterprise));
        when(industryRepository.findAllById(List.of(1L))).thenReturn(List.of(industry));

        var result = service.search("普工", 20);

        assertEquals("制造业工厂", result.get(0).industryName());
        assertEquals("电子厂", result.get(0).enterpriseTypeName());
        assertEquals("普工", result.get(0).name());
    }

    @Test
    void createEnterpriseType_shouldValidateIndustryAndAssignNextSortNo() {
        JobIndustry industry = industry(1L, "INDUSTRY_001", "制造业工厂", 1);
        when(industryRepository.findById(1L)).thenReturn(java.util.Optional.of(industry));
        when(enterpriseRepository.existsByIndustryIdAndNameIgnoreCase(1L, "新企业类型")).thenReturn(false);
        when(enterpriseRepository.findMaxSortNoByIndustryId(1L)).thenReturn(3);
        when(enterpriseRepository.saveAndFlush(any(JobEnterpriseType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createEnterpriseType(1L, " 新企业类型 ", null);

        assertEquals("新企业类型", result.getName());
        assertEquals(4, result.getSortNo());
        assertEquals(1L, result.getIndustryId());
    }

    @Test
    void createJob_shouldRejectEnterpriseFromAnotherIndustry() {
        JobIndustry industry = industry(1L, "INDUSTRY_001", "制造业工厂", 1);
        JobEnterpriseType enterprise = enterprise(11L, 2L, "电子厂", 1);
        when(industryRepository.findById(1L)).thenReturn(java.util.Optional.of(industry));
        when(enterpriseRepository.findById(11L)).thenReturn(java.util.Optional.of(enterprise));

        assertThrows(IllegalArgumentException.class,
                () -> service.createJob(1L, 11L, "普工", null, null));
    }

    @Test
    void createJob_shouldRejectDuplicateNameWithinEnterprise() {
        JobIndustry industry = industry(1L, "INDUSTRY_001", "制造业工厂", 1);
        JobEnterpriseType enterprise = enterprise(11L, 1L, "电子厂", 1);
        when(industryRepository.findById(1L)).thenReturn(java.util.Optional.of(industry));
        when(enterpriseRepository.findById(11L)).thenReturn(java.util.Optional.of(enterprise));
        when(categoryRepository.existsByIndustryIdAndEnterpriseTypeIdAndNameIgnoreCase(1L, 11L, "普工"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.createJob(1L, 11L, "普工", null, null));
    }

    private JobIndustry industry(Long id, String code, String name, int sortNo) {
        JobIndustry item = new JobIndustry();
        item.setId(id);
        item.setCode(code);
        item.setName(name);
        item.setSortNo(sortNo);
        return item;
    }

    private JobEnterpriseType enterprise(Long id, Long industryId, String name, int sortNo) {
        JobEnterpriseType item = new JobEnterpriseType();
        item.setId(id);
        item.setIndustryId(industryId);
        item.setName(name);
        item.setSortNo(sortNo);
        return item;
    }

    private JobCategory job(Long id, Long industryId, Long enterpriseId,
                            String name, String description, int sortNo) {
        JobCategory item = new JobCategory();
        item.setId(id);
        item.setIndustryId(industryId);
        item.setEnterpriseTypeId(enterpriseId);
        item.setName(name);
        item.setDescription(description);
        item.setSortNo(sortNo);
        return item;
    }
}

package com.kuaima.app.domain.jobcategory.model;

import java.util.List;

public final class JobCategoryModels {

    private JobCategoryModels() {
    }

    public record JobItem(Long id, Long industryId, Long enterpriseTypeId,
                          String name, String description, Integer sortNo) {
    }

    public record EnterpriseItem(Long id, String name, Integer sortNo, List<JobItem> jobs) {
    }

    public record IndustryItem(Long id, String code, String name, Integer sortNo,
                               List<EnterpriseItem> enterpriseTypes) {
    }

    public record HotItem(Long id, String name, String displayName,
                          Long jobCategoryId, Integer sortNo, Boolean hot) {
    }

    public record SearchItem(Long id, Long industryId, String industryName,
                             Long enterpriseTypeId, String enterpriseTypeName,
                             String name, String description, Integer sortNo) {
    }
}

package com.kuaima.app.domain.jobcategory.model;

/** 老板端新增企业类型和工种的请求模型。 */
public final class JobCategoryAdminModels {

    private JobCategoryAdminModels() {
    }

    public record CreateEnterpriseTypeRequest(Long industryId, String name, Integer sortNo) {
    }

    public record CreateJobRequest(Long industryId, Long enterpriseTypeId,
                                   String name, String description, Integer sortNo) {
    }
}

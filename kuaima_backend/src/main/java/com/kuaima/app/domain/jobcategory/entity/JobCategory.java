package com.kuaima.app.domain.jobcategory.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** 企业类型下的工种。 */
@Entity
@Table(name = "job_category", uniqueConstraints = {
        @UniqueConstraint(name = "uk_job_category_enterprise_name", columnNames = {"enterprise_type_id", "name"})
})
@Getter
@Setter
public class JobCategory extends BaseEntity {

    @Column(name = "industry_id", nullable = false, comment = "所属行业ID")
    private Long industryId;

    @Column(name = "enterprise_type_id", nullable = false, comment = "所属企业类型ID")
    private Long enterpriseTypeId;

    @Column(nullable = false, length = 100, comment = "工种名称")
    private String name;

    @Column(length = 500, comment = "工种描述关键词")
    private String description;

    @Column(name = "sort_no", nullable = false, comment = "排序号")
    private Integer sortNo;

    @Column(nullable = false, comment = "是否启用")
    private Boolean enabled = true;
}

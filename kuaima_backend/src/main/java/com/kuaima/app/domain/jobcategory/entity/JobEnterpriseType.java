package com.kuaima.app.domain.jobcategory.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** 行业下的企业类型。 */
@Entity
@Table(name = "job_enterprise_type", uniqueConstraints = {
        @UniqueConstraint(name = "uk_job_enterprise_industry_name", columnNames = {"industry_id", "name"})
})
@Getter
@Setter
public class JobEnterpriseType extends BaseEntity {

    @Column(name = "industry_id", nullable = false, comment = "所属行业ID")
    private Long industryId;

    @Column(nullable = false, length = 100, comment = "企业类型名称")
    private String name;

    @Column(name = "sort_no", nullable = false, comment = "排序号")
    private Integer sortNo;

    @Column(nullable = false, comment = "是否启用")
    private Boolean enabled = true;
}

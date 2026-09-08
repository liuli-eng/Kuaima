package com.kuaima.app.domain.jobcategory.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 行业分类。 */
@Entity
@Table(name = "job_industry")
@Getter
@Setter
public class JobIndustry extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100, comment = "行业名称")
    private String name;

    @Column(nullable = false, unique = true, length = 50, comment = "稳定编码")
    private String code;

    @Column(name = "sort_no", nullable = false, comment = "排序号")
    private Integer sortNo;

    @Column(nullable = false, comment = "是否启用")
    private Boolean enabled = true;
}

package com.kuaima.app.domain.jobcategory.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 面向用户展示的热门工种快捷入口。 */
@Entity
@Table(name = "job_category_hot")
@Getter
@Setter
public class JobCategoryHot extends BaseEntity {

    @Column(name = "display_name", nullable = false, unique = true, length = 100, comment = "热门工种展示名称")
    private String displayName;

    @Column(name = "job_category_id", comment = "关联工种ID，可为空")
    private Long jobCategoryId;

    @Column(name = "sort_no", nullable = false, comment = "排序号")
    private Integer sortNo;

    @Column(nullable = false, comment = "是否启用")
    private Boolean enabled = true;
}

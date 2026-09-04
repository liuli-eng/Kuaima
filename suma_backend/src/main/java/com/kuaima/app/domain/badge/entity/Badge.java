package com.kuaima.app.domain.badge.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 徽章目录：平台预设的徽章定义，code 唯一，rule 描述解锁条件。
 */
@Entity
@Table(name = "badge")
@Getter
@Setter
public class Badge extends BaseEntity {

    @Column(length = 50, unique = true, comment = "徽章编码")
    private String code;

    @Column(length = 100, comment = "徽章标题")
    private String title;

    @Column(length = 200, comment = "徽章图标URL")
    private String iconUrl;

    @Column(length = 500, comment = "徽章描述")
    private String description;

    @Column(length = 200, comment = "解锁规则描述")
    private String rule;
}

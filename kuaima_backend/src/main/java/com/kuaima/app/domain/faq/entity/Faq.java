package com.kuaima.app.domain.faq.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 常见问题：客服中心 FAQ。
 */
@Entity
@Table(name = "faq")
@Getter
@Setter
public class Faq extends BaseEntity {

    @Column(length = 200, comment = "问题")
    private String question;

    @Column(columnDefinition = "TEXT", comment = "答案")
    private String answer;

    @Column(length = 50, comment = "分类")
    private String category;

    @Column(comment = "排序值(小在前)")
    private Integer sortOrder;

    @Column(comment = "是否启用")
    private Boolean enabled;
}

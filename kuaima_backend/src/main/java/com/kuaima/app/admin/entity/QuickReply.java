package com.kuaima.app.admin.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 客服快捷回复：预设回复模板，提高客服效率。
 */
@Entity
@Table(name = "quick_reply")
@Getter
@Setter
public class QuickReply extends BaseEntity {

    @Column(length = 500, comment = "回复内容")
    private String content;

    @Column(length = 50, comment = "分类")
    private String category;

    @Column(comment = "排序值(小在前)")
    private Integer sortOrder;

    @Column(comment = "是否启用")
    private Boolean enabled;
}

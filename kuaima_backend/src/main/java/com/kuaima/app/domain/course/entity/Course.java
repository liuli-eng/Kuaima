package com.kuaima.app.domain.course.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 培训课程：零工入职/合规培训，含视频与小测。
 */
@Entity
@Table(name = "course")
@Getter
@Setter
public class Course extends BaseEntity {

    @Column(length = 100, comment = "课程标题")
    private String title;

    @Column(length = 50, comment = "课程分类")
    private String category;

    @Column(length = 200, comment = "封面图URL")
    private String coverUrl;

    @Column(length = 500, comment = "课程简介")
    private String intro;

    @Column(comment = "排序值(小在前)")
    private Integer sortOrder;

    @Column(length = 20, comment = "状态: 上架/下架")
    private String status;
}

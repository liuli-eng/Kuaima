package com.kuaima.app.domain.course.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 课程视频：单条视频资源，按 sortOrder 顺序播放。
 */
@Entity
@Table(name = "course_video")
@Getter
@Setter
public class CourseVideo extends BaseEntity {

    @Column(comment = "所属课程ID")
    private Long courseId;

    @Column(length = 100, comment = "视频标题")
    private String title;

    @Column(length = 200, comment = "视频URL")
    private String videoUrl;

    @Column(comment = "视频时长(秒)")
    private Integer duration;

    @Column(comment = "排序值(小在前)")
    private Integer sortOrder;
}

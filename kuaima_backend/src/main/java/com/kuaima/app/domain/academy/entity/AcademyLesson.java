package com.kuaima.app.domain.academy.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 新手「如何接单」课程槽位。视频为空表示待上传。 */
@Entity
@Table(name = "academy_lesson")
@Getter
@Setter
public class AcademyLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String lessonKey;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 500)
    private String video;

    private Integer duration;

    private Long size;

    @Column(length = 10)
    private String ext;

    @Column(nullable = false)
    private Long learners = 0L;

    @Column(nullable = false)
    private Boolean enabled = true;

    private LocalDateTime uploadedAt;
}

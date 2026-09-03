package com.suma.app.admin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_banner")
@Getter
@Setter
public class Banner {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 200)
    private String imageUrl;

    @Column(length = 50)
    private String position;   // 位置：worker-home / boss-home / admin-home

    @Column
    private Integer weight = 0; // 排序权重

    @Column(length = 200)
    private String linkUrl;

    @Column(length = 20)
    private String status = "展示中"; // 展示中 / 草稿 / 已下架

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;
}

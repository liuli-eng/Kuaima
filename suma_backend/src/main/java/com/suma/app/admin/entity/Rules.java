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
@Table(name = "admin_rules")
@Getter
@Setter
public class Rules {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    /** 分类：通知公告 / 信用评定 / 收费标准 / 交易规则 / 隐私协议 */
    @Column(length = 30)
    private String category;

    @Column(length = 30)
    private String version = "v1.0";

    @Column(length = 20)
    private String status = "草稿"; // 草稿 / 已发布 / 已归档

    @Column
    private LocalDateTime effectiveTime;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;
}

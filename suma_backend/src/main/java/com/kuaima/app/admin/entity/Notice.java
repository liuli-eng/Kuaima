package com.kuaima.app.admin.entity;

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
@Table(name = "admin_notice")
@Getter
@Setter
public class Notice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    /** 类型：系统 / 活动 / 政策 */
    @Column(length = 20)
    private String type;

    /** 发布范围：全部 / 零工 / 雇主 */
    @Column(length = 20)
    private String scope = "全部";

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 20)
    private String status = "已发布"; // 已发布 / 草稿 / 已下架

    @Column
    private LocalDateTime publishTime;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;
}

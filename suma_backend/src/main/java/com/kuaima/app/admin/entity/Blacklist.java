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

/** 黑名单 */
@Entity
@Table(name = "admin_blacklist")
@Getter @Setter
public class Blacklist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 20)
    private String userType;    // 零工 / 雇主

    @Column(length = 50)
    private String reason;

    @Column(length = 20)
    private String status = "封禁中"; // 封禁中 / 已解封

    @Column
    private LocalDateTime expireTime;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;
}

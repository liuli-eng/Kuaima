package com.kuaima.app.admin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 公告已读记录：标记某个管理员已读过某条公告，用于登录后未读公告浮层。
 * 唯一约束 (adminUserId, noticeId) 防止重复标记。
 */
@Entity
@Table(name = "admin_notice_read", uniqueConstraints = {
        @UniqueConstraint(name = "uk_admin_notice", columnNames = { "adminUserId", "noticeId" })
})
@Getter
@Setter
public class NoticeRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminUserId;

    @Column(nullable = false)
    private Long noticeId;

    @Column
    private LocalDateTime readTime = LocalDateTime.now();
}

package com.kuaima.app.domain.badge.entity;

import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户已解锁徽章：解锁时写入一条记录，unlockedAt 记录解锁时间。
 */
@Entity
@Table(name = "user_badge")
@Getter
@Setter
public class UserBadge extends BaseEntity {

    @Column(comment = "用户ID")
    private Long userId;

    @Column(comment = "徽章ID")
    private Long badgeId;

    @Column(comment = "解锁时间")
    private Timestamp unlockedAt;
}

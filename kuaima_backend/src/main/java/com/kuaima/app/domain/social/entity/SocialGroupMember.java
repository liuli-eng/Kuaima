package com.kuaima.app.domain.social.entity;

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

/** 当前用户确认加入社群的记录；同一用户对同一群幂等。 */
@Entity
@Table(name = "social_group_member", uniqueConstraints = @UniqueConstraint(name = "uk_social_group_member", columnNames = {"group_id", "user_id"}))
@Getter
@Setter
public class SocialGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 100)
    private String source;

    @Column(nullable = false)
    private Boolean confirmed = false;

    @Column(nullable = false)
    private LocalDateTime joinedAt;
}

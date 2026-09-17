package com.kuaima.app.domain.boss.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "boss_recruit_account",
        uniqueConstraints = @UniqueConstraint(name = "uk_boss_recruit_owner_target",
                columnNames = {"owner_user_id", "target_user_id"}),
        indexes = @Index(name = "idx_boss_recruit_target_user", columnList = "target_user_id"))
@Getter
@Setter
public class BossRecruitAccount extends BaseEntity {
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(nullable = false)
    private String name;

    private String avatar;

    @Column(length = 20)
    private String authorizationType = "PERSONAL";

    @Column(length = 20)
    private String workCode;

    @Column(length = 20)
    private String leaveCode;

    private Boolean current = false;

    @PrePersist
    void initializeTargetUserId() {
        if (targetUserId == null) targetUserId = ownerUserId;
    }
}

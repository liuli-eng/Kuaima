package com.kuaima.app.domain.invite.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 邀请码：每个用户拥有一个唯一邀请码，首次获取时自动生成 8 位随机码。
 */
@Entity
@Table(name = "invite_code")
@Getter
@Setter
public class InviteCode extends BaseEntity {

    @Column(comment = "用户ID")
    private Long userId;

    @Column(length = 50, unique = true, comment = "邀请码")
    private String code;
}

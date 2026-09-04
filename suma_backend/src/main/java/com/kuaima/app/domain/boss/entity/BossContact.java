package com.kuaima.app.domain.boss.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 老板常用联系人（发布岗位/录用后联系零工使用）。
 */
@Entity
@Table(name = "boss_contact")
@Getter
@Setter
public class BossContact extends BaseEntity {

    @Column(comment = "所属老板用户 id")
    private Long userId;

    @Column(length = 50, comment = "联系人姓名")
    private String name;

    @Column(length = 20, comment = "联系电话")
    private String phone;

    @Column(comment = "是否默认联系人")
    private Boolean isDefault;
}

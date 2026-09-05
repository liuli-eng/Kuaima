package com.kuaima.app.domain.social.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 社群群组：用户可加入的微信/QQ群。
 */
@Entity
@Table(name = "social_group")
@Getter
@Setter
public class SocialGroup extends BaseEntity {

    @Column(length = 100, comment = "群名称")
    private String name;

    @Column(length = 50, comment = "分类:零工/老板/交流等")
    private String category;

    @Column(length = 500, comment = "群二维码URL")
    private String qrcodeUrl;

    @Column(comment = "成员数")
    private Integer memberCount;
}

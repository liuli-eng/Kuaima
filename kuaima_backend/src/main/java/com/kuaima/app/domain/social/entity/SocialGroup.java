package com.kuaima.app.domain.social.entity;

import java.time.LocalDateTime;

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
    private Integer memberCount = 0;

    @Column(length = 500, comment = "群简介")
    private String description;

    @Column(length = 20, comment = "可见角色: WORKER/BOSS")
    private String role = "WORKER";

    @Column(length = 20, comment = "群状态: ACTIVE/FULL/DISABLED")
    private String status = "ACTIVE";

    @Column(comment = "人数上限")
    private Integer memberLimit = 200;

    @Column(comment = "建群时间")
    private LocalDateTime createdAt;

    @Column(comment = "展示排序")
    private Integer sort = 0;

    @Column(nullable = false, comment = "是否已删除")
    private Boolean deleted = false;
}

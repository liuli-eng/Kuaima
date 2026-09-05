package com.kuaima.app.domain.boss.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 老板常用招工地址。
 */
@Entity
@Table(name = "boss_address")
@Getter
@Setter
public class BossAddress extends BaseEntity {

    @Column(comment = "所属老板用户 id")
    private Long userId;

    @Column(length = 100, comment = "地址名称")
    private String name;

    @Column(length = 200, comment = "地址详情")
    private String detail;

    @Column(comment = "纬度")
    private Double lat;

    @Column(comment = "经度")
    private Double lng;

    @Column(comment = "是否默认地址")
    private Boolean isDefault;
}

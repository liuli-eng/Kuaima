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

    @Column(name = "enterprise_id", comment = "所属企业")
    private Long enterpriseId;

    @Column(comment = "所属老板用户 id")
    private Long userId;

    @Column(length = 100, comment = "地址名称")
    private String name;

    @Column(length = 50, comment = "联系人姓名")
    private String contactName;

    @Column(length = 20, comment = "联系人手机号")
    private String contactPhone;

    @Column(length = 50, comment = "城市")
    private String city;

    @Column(length = 50, comment = "区县")
    private String district;

    @Column(length = 200, comment = "地址详情")
    private String detail;

    @Column(comment = "纬度")
    private Double lat;

    @Column(comment = "经度")
    private Double lng;

    /** 地址接口统一返回字段；保留 lat/lng 兼容已有调用方。 */
    @com.fasterxml.jackson.annotation.JsonProperty("latitude")
    public Double getLatitude() { return lat; }

    @com.fasterxml.jackson.annotation.JsonProperty("longitude")
    public Double getLongitude() { return lng; }

    @Column(comment = "是否默认地址")
    private Boolean isDefault;
}

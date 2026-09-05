package com.kuaima.app.domain.reward.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 积分兑换商品：用户使用积分兑换的实物/虚拟奖品。
 */
@Entity
@Table(name = "reward")
@Getter
@Setter
public class Reward extends BaseEntity {

    @Column(length = 100, comment = "奖品名称")
    private String title;

    @Column(length = 500, comment = "奖品描述")
    private String description;

    @Column(comment = "兑换所需积分")
    private Integer pointsCost;

    @Column(comment = "库存")
    private Integer stock;

    @Column(length = 200, comment = "封面图URL")
    private String coverUrl;

    @Column(length = 20, comment = "状态:上架(UP)/下架(DOWN)")
    private String status;
}

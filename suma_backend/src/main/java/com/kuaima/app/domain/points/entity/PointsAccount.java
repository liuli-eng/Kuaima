package com.kuaima.app.domain.points.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户积分账户：记录可用积分余额。首次查询时自动创建余额 0 的账户。
 */
@Entity
@Table(name = "points_account")
@Getter
@Setter
public class PointsAccount extends BaseEntity {

    @Column(unique = true, comment = "账户所属用户")
    private Long userId;

    @Column(comment = "可用积分余额")
    private Integer balance;
}

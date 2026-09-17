package com.kuaima.app.domain.points.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户积分账户：记录某个身份下的可用积分余额。首次查询时自动创建余额 0 的账户。
 */
@Entity
@Table(name = "points_account",
        uniqueConstraints = @UniqueConstraint(name = "uk_points_account_user_role",
                columnNames = {"user_id", "role"}))
@Getter
@Setter
public class PointsAccount extends BaseEntity {

    @Column(comment = "账户所属用户")
    private Long userId;

    /** 积分所属身份：BOSS（老板）/ USER（零工）。 */
    @Column(length = 20, nullable = false)
    private String role = "BOSS";

    @Column(comment = "可用积分余额")
    private Integer balance;
}

package com.kuaima.app.domain.points.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 积分流水：每次积分变动（增加/扣减）记录一条流水，bizType 标识业务来源。
 */
@Entity
@Table(name = "points_flow")
@Getter
@Setter
public class PointsFlow extends BaseEntity {

    @Column(comment = "用户ID")
    private Long userId;

    @Column(comment = "变动积分(正数加/负数减)")
    private Integer delta;

    @Column(length = 50, comment = "业务类型:如 SIGN_IN/REWARD_EXCHANGE/INVITE 等")
    private String bizType;

    @Column(length = 200, comment = "备注")
    private String remark;
}

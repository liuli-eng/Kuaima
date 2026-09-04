package com.kuaima.app.domain.reward.entity;

import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 积分兑换记录：用户兑换奖品后生成的兑换单，含发货状态。
 */
@Entity
@Table(name = "reward_exchange")
@Getter
@Setter
public class RewardExchange extends BaseEntity {

    @Column(comment = "兑换用户ID")
    private Long userId;

    @Column(comment = "兑换的奖品ID")
    private Long rewardId;

    @Column(length = 20, comment = "状态:待发货(PENDING)/已发货(SHIPPED)/已完成(DONE)")
    private String status;

    @Column(comment = "发货时间")
    private Timestamp shippedAt;
}

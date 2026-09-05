package com.kuaima.app.domain.contract.entity;

import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 合同：老板与零工之间的电子合同。
 */
@Entity
@Table(name = "contract")
@Getter
@Setter
public class Contract extends BaseEntity {

    @Column(comment = "老板用户ID")
    private Long bossId;

    @Column(comment = "零工用户ID")
    private Long workerId;

    @Column(comment = "关联订单ID")
    private Long orderId;

    @Column(length = 20, comment = "状态:待签署(PENDING)/已签署(SIGNED)")
    private String status;

    @Column(comment = "签署时间")
    private Timestamp signedAt;

    @Column(columnDefinition = "TEXT", comment = "合同内容")
    private String content;
}

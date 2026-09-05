package com.kuaima.app.domain.insurance.entity;

import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 保险记录：用户为某岗位投保的保单。
 */
@Entity
@Table(name = "insurance")
@Getter
@Setter
public class Insurance extends BaseEntity {

    @Column(comment = "投保用户ID")
    private Long userId;

    @Column(length = 20, comment = "用户类型: WORKER/BOSS")
    private String userType;

    @Column(comment = "关联订单ID")
    private Long orderId;

    @Column(length = 50, comment = "保险类型:意外险/医疗险等")
    private String type;

    @Column(comment = "保额(元)")
    private Double amount;

    @Column(comment = "保费(元)")
    private Double premium;

    @Column(length = 20, comment = "状态:生效中(ACTIVE)/已失效(EXPIRED)/理赔中(CLAIMING)")
    private String status;

    @Column(comment = "生效时间")
    private Timestamp startTime;

    @Column(comment = "失效时间")
    private Timestamp endTime;
}

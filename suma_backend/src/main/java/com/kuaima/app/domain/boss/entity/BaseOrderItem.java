package com.kuaima.app.domain.boss.entity;

import java.sql.Date;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "boss_order_item")
@Getter
@Setter
public class BaseOrderItem extends BaseEntity {
    
    @Column
    private Long orderId;

    @Column(comment = "报名用户")
    private Long userId;

    @Column(comment = "报名状态:已报名，已录用，已到岗，已完成，取消招工，取消报名")
    private String status;

    @Column(comment = "我要试工：仅月结订单报名时可勾选")
    private Boolean trialRequested;

    @Column(comment = "报名备注")
    private String remark;

    @Column(comment = "报名时间")
    private Date applyDate;

    @Column(comment = "录用时间")
    private Date hireDate;

    @Column(comment = "到岗时间")
    private Date workDate;

    @Column(comment = "完成时间")
    private Date finishDate;

    @Column(comment = "取消时间")
    private Date cancelDate;

    @Column(comment = "取消原因")
    private String cancelReason;


}

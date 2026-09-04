package com.kuaima.app.domain.deposit.entity;

import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 押金单：用户支付押金并记录流水，可申请退款。
 */
@Entity
@Table(name = "deposit")
@Getter
@Setter
public class Deposit extends BaseEntity {

    @Column(comment = "用户ID")
    private Long userId;

    @Column(comment = "押金金额(元)")
    private Double amount;

    @Column(length = 20, comment = "状态:待支付(PENDING)/已支付(PAID)/已退款(REFUNDED)")
    private String status;

    @Column(comment = "支付时间")
    private Timestamp payTime;

    @Column(comment = "退款时间")
    private Timestamp refundTime;
}

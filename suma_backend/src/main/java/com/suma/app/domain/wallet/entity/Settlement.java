package com.suma.app.domain.wallet.entity;

import java.time.LocalDateTime;

import com.suma.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 结算支付单：老板对一条"已完成"的报名记录发起结算，
 * 系统按 订单工资 × 工作天数 算出应付零工工资(分)，并叠加平台服务费(分)，生成待支付单。
 * 支付成功后工资计入零工钱包；服务费归平台，仅记录在结算单上，不流转到个人钱包。
 */
@Entity
@Table(name = "boss_settlement")
@Getter
@Setter
public class Settlement extends BaseEntity {

    @Column(comment = "报名记录 id")
    private Long itemId;

    @Column(comment = "招工订单 id")
    private Long orderId;

    @Column(comment = "收款零工用户 id")
    private Long workerId;

    @Column(comment = "结算工作天数")
    private Integer workDays;

    @Column(comment = "应付零工工资(分)=订单工资×工作天数")
    private Long wage;

    @Column(comment = "平台服务费(分)，费率规则待定，默认 0")
    private Long serviceFee;

    @Column(comment = "老板实付总额(分)=工资+服务费")
    private Long totalAmount;

    @Column(comment = "结算单状态:待支付/已支付/已取消")
    private String status;

    @Column(comment = "支付流水号(模拟)")
    private String payNo;

    @Column(comment = "支付时间")
    private LocalDateTime payTime;
}

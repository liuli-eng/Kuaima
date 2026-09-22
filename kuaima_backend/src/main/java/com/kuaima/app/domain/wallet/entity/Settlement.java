package com.kuaima.app.domain.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 结算支付单：老板对一条"已完成"的报名记录发起结算，
 * 系统按订单工资 × 工作天数算出应付零工工资（元），并叠加平台服务费（元），生成待支付单。
 * 支付成功后工资计入零工钱包；服务费归平台，仅记录在结算单上，不流转到个人钱包。
 */
@Entity
@Table(name = "boss_settlement")
@Getter
@Setter
public class Settlement extends BaseEntity {
    public void setWage(Long value) { this.wage = value == null ? null : BigDecimal.valueOf(value); }
    public void setWage(long value) { this.wage = BigDecimal.valueOf(value); }
    public void setWage(BigDecimal value) { this.wage = value; }
    public void setServiceFee(Long value) { this.serviceFee = value == null ? null : BigDecimal.valueOf(value); }
    public void setServiceFee(long value) { this.serviceFee = BigDecimal.valueOf(value); }
    public void setServiceFee(BigDecimal value) { this.serviceFee = value; }
    public void setTotalAmount(Long value) { this.totalAmount = value == null ? null : BigDecimal.valueOf(value); }
    public void setTotalAmount(long value) { this.totalAmount = BigDecimal.valueOf(value); }
    public void setTotalAmount(BigDecimal value) { this.totalAmount = value; }

    @Column(comment = "报名记录 id")
    private Long itemId;

    @Column(comment = "招工订单 id")
    private Long orderId;

    @Column(comment = "收款零工用户 id")
    private Long workerId;

    @Column(comment = "结算工作天数")
    private Integer workDays;

    @Column(precision = 18, scale = 2, comment = "应付零工工资（元）=订单工资×工作天数")
    private BigDecimal wage;

    @Column(precision = 18, scale = 2, comment = "平台服务费（元）")
    private BigDecimal serviceFee;

    @Column(precision = 18, scale = 2, comment = "老板实付总额（元）=工资+服务费")
    private BigDecimal totalAmount;

    @Column(comment = "结算单状态:待支付/已支付/已取消")
    private String status;

    @Column(comment = "支付流水号（微信交易号或模拟流水号）")
    private String payNo;

    @Column(name = "wechat_transaction_id", length = 64, comment = "微信支付交易号")
    private String wechatTransactionId;

    @Column(comment = "支付时间")
    private LocalDateTime payTime;
}

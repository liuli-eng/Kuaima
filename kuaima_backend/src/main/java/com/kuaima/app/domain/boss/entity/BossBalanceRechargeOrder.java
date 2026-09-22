package com.kuaima.app.domain.boss.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 老板商户余额充值订单。支付方式：wechat / alipay；金额以「元」存储。 */
@Entity
@Table(name = "boss_balance_recharge_order", indexes = {
        @Index(name = "idx_bbr_boss", columnList = "bossId"),
        @Index(name = "idx_bbr_status", columnList = "status"),
        @Index(name = "idx_bbr_method", columnList = "payMethod")
})
@Setter
@Getter
public class BossBalanceRechargeOrder extends BaseEntity {

    @Column(nullable = false, length = 40, unique = true)
    private String orderNo;

    @Column(length = 128, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long bossId;

    @Column(length = 100)
    private String bossName;

    @Column(length = 150)
    private String companyName;

    @Column
    private Long accountId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /** wechat / alipay */
    @Column(nullable = false, length = 20)
    private String payMethod;

    /** pending / paid / failed / canceled / manual_pending / manual_confirmed */
    @Column(nullable = false, length = 20)
    private String status = "pending";

    private Timestamp payTime;

    @Column(length = 64)
    private String transactionId;

    @Column(length = 500)
    private String payQrUrl;

    @Column(length = 500)
    private String payUrl;

    @Column(length = 4000)
    private String payParams;

    @Column(length = 500)
    private String remark;

    private Long operatorId;

    @Column(length = 100)
    private String operatorName;

    private Timestamp operatorTime;

    @Column(length = 500)
    private String operatorRemark;
}

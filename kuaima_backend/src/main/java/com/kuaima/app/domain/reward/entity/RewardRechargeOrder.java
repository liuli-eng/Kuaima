package com.kuaima.app.domain.reward.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** 微信支付奖励金充值订单；金额单位为元。 */
@Entity
@Table(name = "reward_recharge_order", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reward_recharge_order_no", columnNames = "order_no"),
        @UniqueConstraint(name = "uk_reward_recharge_idempotency", columnNames = "idempotency_key")
}, indexes = {
        @Index(name = "idx_reward_recharge_user_time", columnList = "user_id,created_at"),
        @Index(name = "idx_reward_recharge_status_time", columnList = "status,created_at")
})
@Getter
@Setter
public class RewardRechargeOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 48)
    private String orderNo;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, length = 20)
    private String role = "BOSS";

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "bonus_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal bonusAmount = BigDecimal.ZERO;

    @Column(name = "pay_method", nullable = false, length = 30)
    private String payMethod = "WECHAT";

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "pay_params", length = 4000)
    private String payParams;

    @Column(name = "reward_credited", nullable = false)
    private Boolean rewardCredited = false;

    @Column(name = "wechat_transaction_id", length = 64)
    private String wechatTransactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;
}

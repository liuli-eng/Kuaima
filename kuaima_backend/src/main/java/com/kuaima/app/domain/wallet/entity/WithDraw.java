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
 * 提现单：零工将钱包余额提现到自己的账户（模拟打款，后续接微信商家转账）。
 * 申请时先扣减钱包余额，打款成功即完成；打款失败则余额退回钱包。
 */
@Entity
@Table(name = "with_draw", uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        name = "uk_wallet_withdraw_idempotency", columnNames = "idempotency_key"))
@Getter
@Setter
public class WithDraw extends BaseEntity {
    public void setAmount(Long value) { this.amount = value == null ? null : BigDecimal.valueOf(value); }
    public void setAmount(long value) { this.amount = BigDecimal.valueOf(value); }
    public void setAmount(BigDecimal value) { this.amount = value; }

    @Column(comment = "提现用户")
    private Long userId;

    @Column(nullable = false, length = 20, comment = "账户身份: BOSS/USER")
    private String role = "USER";

    @Column(precision = 18, scale = 2, comment = "提现金额（元）")
    private BigDecimal amount;

    @Column(comment = "提现状态:申请中/已打款/打款失败")
    private String status;

    @Column(comment = "提现渠道:模拟(mock)，后续接微信商家转账")
    private String channel;

    @Column(comment = "收款账户(预留，如微信 openid/银行卡)")
    private String account;

    @Column(comment = "申请时间")
    private LocalDateTime applyTime;

    @Column(comment = "打款时间")
    private LocalDateTime payTime;

    @Column(comment = "失败原因/备注")
    private String remark;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "merchant_batch_no", length = 64)
    private String merchantBatchNo;

    @Column(name = "merchant_detail_no", length = 64)
    private String merchantDetailNo;

    @Column(name = "wechat_transfer_no", length = 64)
    private String wechatTransferNo;

    @Column(name = "transfer_response", columnDefinition = "TEXT")
    private String transferResponse;
}

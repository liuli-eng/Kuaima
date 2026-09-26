package com.kuaima.app.domain.wallet.entity;

import java.math.BigDecimal;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 钱包流水（账务明细）：金额统一以元（BigDecimal，两位小数）存储。
 */
@Entity
@Table(name = "wallet_flow")
@Getter
@Setter
public class WalletFlow extends BaseEntity {
    public void setAmount(Long value) { this.amount = value == null ? null : BigDecimal.valueOf(value); }
    public void setAmount(long value) { this.amount = BigDecimal.valueOf(value); }
    public void setAmount(BigDecimal value) { this.amount = value; }
    public void setBalanceAfter(Long value) { this.balanceAfter = value == null ? null : BigDecimal.valueOf(value); }
    public void setBalanceAfter(long value) { this.balanceAfter = BigDecimal.valueOf(value); }
    public void setBalanceAfter(BigDecimal value) { this.balanceAfter = value; }

    @Column(comment = "钱包所属用户")
    private Long userId;

    @Column(nullable = false, length = 20, comment = "账户身份: BOSS/USER")
    private String role = "USER";

    @Column(comment = "方向:income(收入)/outcome(支出)")
    private String direction;

    @Column(comment = "业务类型:WAGE(工资入账)/WITHDRAW(提现)/WITHDRAW_REFUND(提现退回)")
    private String bizType;

    @Column(precision = 18, scale = 2, comment = "变动金额（元），正数")
    private BigDecimal amount;

    @Column(precision = 18, scale = 2, comment = "变动后余额（元）")
    private BigDecimal balanceAfter;

    @Column(comment = "关联业务 id:结算单/提现单 id")
    private Long bizId;

    @Column(comment = "备注")
    private String remark;
}

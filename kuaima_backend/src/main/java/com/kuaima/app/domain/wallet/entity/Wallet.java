package com.kuaima.app.domain.wallet.entity;

import java.math.BigDecimal;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户钱包：可提现余额统一以元（BigDecimal，两位小数）存储。
 */
@Entity
@Table(name = "wallet", uniqueConstraints = @jakarta.persistence.UniqueConstraint(name = "uk_wallet_user_role", columnNames = {"user_id", "role"}))
@Getter
@Setter
public class Wallet extends BaseEntity {
    public void setBalance(Long value) { this.balance = value == null ? null : BigDecimal.valueOf(value); }
    public void setBalance(long value) { this.balance = BigDecimal.valueOf(value); }
    public void setBalance(BigDecimal value) { this.balance = value; }

    @Column(name = "user_id", nullable = false, comment = "钱包所属用户")
    private Long userId;

    @Column(nullable = false, length = 20, comment = "账户身份: BOSS/USER")
    private String role = "USER";

    @Column(precision = 18, scale = 2, comment = "可用余额（元）")
    private BigDecimal balance = BigDecimal.ZERO;
}

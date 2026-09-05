package com.kuaima.app.domain.wallet.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户钱包：可提现余额以分(Long)存储，避免浮点误差（与微信支付分单位一致）。
 */
@Entity
@Table(name = "wallet")
@Getter
@Setter
public class Wallet extends BaseEntity {

    @Column(unique = true, comment = "钱包所属用户")
    private Long userId;

    @Column(comment = "可用余额(分)")
    private Long balance;
}

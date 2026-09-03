package com.suma.app.domain.wallet.entity;

import com.suma.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 钱包流水（账务明细）：方向 income/outcome + 业务类型 + 变动金额(分) + 变动后余额(分)
 */
@Entity
@Table(name = "wallet_flow")
@Getter
@Setter
public class WalletFlow extends BaseEntity {

    @Column(comment = "钱包所属用户")
    private Long userId;

    @Column(comment = "方向:income(收入)/outcome(支出)")
    private String direction;

    @Column(comment = "业务类型:WAGE(工资入账)/WITHDRAW(提现)/WITHDRAW_REFUND(提现退回)")
    private String bizType;

    @Column(comment = "变动金额(分)，正数")
    private Long amount;

    @Column(comment = "变动后余额(分)")
    private Long balanceAfter;

    @Column(comment = "关联业务 id:结算单/提现单 id")
    private Long bizId;

    @Column(comment = "备注")
    private String remark;
}

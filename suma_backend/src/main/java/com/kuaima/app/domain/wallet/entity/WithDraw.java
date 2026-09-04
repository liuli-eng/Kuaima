package com.kuaima.app.domain.wallet.entity;

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
@Table(name = "with_draw")
@Getter
@Setter
public class WithDraw extends BaseEntity {

    @Column(comment = "提现用户")
    private Long userId;

    @Column(comment = "提现金额(分)")
    private Long amount;

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
}

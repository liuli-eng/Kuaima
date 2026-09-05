package com.kuaima.app.domain.wallet.constant;

/**
 * 结算单状态常量
 */
public final class SettlementStatus {

    private SettlementStatus() {
    }

    /** 待支付（老板发起结算、尚未完成支付） */
    public static final String PENDING = "待支付";

    /** 已支付（模拟支付成功，工资已入零工钱包） */
    public static final String PAID = "已支付";

    /** 已取消 */
    public static final String CANCELED = "已取消";
}

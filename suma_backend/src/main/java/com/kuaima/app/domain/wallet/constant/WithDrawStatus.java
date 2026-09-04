package com.kuaima.app.domain.wallet.constant;

/**
 * 提现单状态常量
 */
public final class WithDrawStatus {

    private WithDrawStatus() {
    }

    /** 申请中（余额已扣除，待平台打款） */
    public static final String PENDING = "申请中";

    /** 已打款（模拟打款成功，后续可替换为微信商家转账） */
    public static final String SUCCESS = "已打款";

    /** 打款失败（余额退回零工钱包） */
    public static final String FAILED = "打款失败";
}

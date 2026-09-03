package com.suma.app.domain.message.constant;

/**
 * 消息关联的业务对象类型（配合 bizId 跳转到对应业务详情页）。
 */
public final class BizType {

    /** 招工订单(点进订单详情) */
    public static final String ORDER = "order";

    /** 报名记录(点进报名/录用详情) */
    public static final String ITEM = "item";

    /** 结算单(点进工资明细) */
    public static final String SETTLE = "settle";

    /** 提现单(点进提现详情) */
    public static final String WITHDRAW = "withdraw";

    private BizType() {
    }
}

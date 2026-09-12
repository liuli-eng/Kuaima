package com.kuaima.app.domain.message.model;

import java.time.LocalDateTime;
import java.util.List;

/** 老板消息首页聚合数据及历史消息分页返回模型。 */
public final class BossMessageModels {
    private BossMessageModels() {}

    public record Summary(
            String enterpriseStatus,
            boolean enterpriseCertUnread,
            long unreadCount,
            long signupUnreadCount,
            long signupCount,
            MessagePreview latestSystemNotice,
            MessagePreview latestSignup,
            SettlementPreview latestSettlement,
            List<MessageItem> items) {}

    /** 消息首页按实际存在的提醒动态返回的卡片。 */
    public record MessageItem(String key, String title, String content, String type,
                              boolean unread, String action, Long bizId,
                              LocalDateTime createTime) {}

    public record MessagePreview(Long id, String title, String content, String type,
                                 Boolean readFlag, LocalDateTime createTime, Long bizId) {}

    public record SettlementPreview(Long id, String status, Long wage, Long totalAmount,
                                    LocalDateTime payTime, Long orderId, Long itemId) {}
}

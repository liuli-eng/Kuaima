package com.kuaima.app.domain.chat.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户消息通知偏好设置：可控制订单/活动/系统通知开关与提示音、震动。
 * 首次查询时不存在自动创建默认记录。
 */
@Entity
@Table(name = "chat_notification_setting")
@Getter
@Setter
public class NotificationSetting extends BaseEntity {

    @Column(unique = true, comment = "用户ID")
    private Long userId;

    @Column(comment = "订单通知开关")
    private Boolean orderNotif;

    @Column(comment = "活动通知开关")
    private Boolean activityNotif;

    @Column(comment = "系统通知开关")
    private Boolean systemNotif;

    @Column(comment = "提示音开关")
    private Boolean sound;

    @Column(comment = "震动开关")
    private Boolean vibrate;
}

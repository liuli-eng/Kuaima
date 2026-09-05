package com.kuaima.app.admin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 消息模板（短信/站内信触发模板） */
@Entity
@Table(name = "message_template")
@Getter
@Setter
public class MessageTemplate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    /** 触发事件：order_success / order_cancel / register / realname_approved / realname_rejected / settlement / subsidy / system */
    @Column(length = 30)
    private String event;

    /** 触发场景（人类可读）：接单成功 / 工资结算 / 审核结果 / 系统公告 / 飞单提醒 / 手动发送 */
    @Column(length = 30)
    private String scene;

    /** 发送渠道：inapp / sms / both */
    @Column(length = 10)
    private String channel = "both";

    @Column(columnDefinition = "TEXT")
    private String content;

    /** 状态：enabled / disabled */
    @Column(length = 10)
    private String status = "enabled";

    /** 发送方式：即时 / 定时 */
    @Column(length = 10)
    private String sendWay = "即时";

    /** 发送时间：全天 / 工作时间 */
    @Column(length = 20)
    private String sendTime = "全天";

    /** 频率限制：5 / 10 / 不限制 */
    @Column(length = 10)
    private String freqLimit = "5";

    /** 最近使用时间 */
    @Column
    private LocalDateTime lastUsed;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime updateTime;
}

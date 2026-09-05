package com.kuaima.app.domain.message.entity;

import java.time.LocalDateTime;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 站内消息（收件箱）。
 * 事件发生时写入一条记录，前端轮询 未读数/列表 拉取。
 */
@Entity
@Table(name = "sys_message")
@Getter
@Setter
public class Message extends BaseEntity {

    @Column(comment = "接收者用户ID")
    private Long userId;

    @Column(comment = "接收者角色: BOSS/USER")
    private String role;

    @Column(comment = "消息类型:见 MessageType")
    private String type;

    @Column(comment = "标题")
    private String title;

    @Column(length = 500, comment = "正文")
    private String content;

    @Column(comment = "关联业务类型:order/item/settle/withdraw,见 BizType")
    private String bizType;

    @Column(comment = "关联业务ID")
    private Long bizId;

    @Column(comment = "是否已读: false-未读 true-已读")
    private Boolean readFlag;

    @Column(comment = "已读时间")
    private LocalDateTime readTime;

    @Column(comment = "消息发生时间")
    private LocalDateTime createTime;
}

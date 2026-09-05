package com.kuaima.app.domain.chat.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 客服会话单条消息：USER/AGENT 双向，TEXT/IMAGE 多类型。
 */
@Entity
@Table(name = "chat_message")
@Getter
@Setter
public class ChatMessage extends BaseEntity {

    @Column(comment = "所属会话ID")
    private Long sessionId;

    @Column(comment = "发送者ID")
    private Long fromId;

    @Column(length = 20, comment = "发送者类型: USER/AGENT")
    private String fromType;

    @Column(length = 500, comment = "消息内容")
    private String content;

    @Column(length = 20, comment = "内容类型: TEXT/IMAGE")
    private String contentType;
}

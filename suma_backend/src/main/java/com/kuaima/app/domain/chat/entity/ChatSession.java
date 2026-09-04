package com.kuaima.app.domain.chat.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 在线客服会话：用户发起 -> 自动分配客服(AGENT) -> 多轮消息交互。
 */
@Entity
@Table(name = "chat_session")
@Getter
@Setter
public class ChatSession extends BaseEntity {

    @Column(comment = "发起会话用户ID")
    private Long userId;

    @Column(comment = "客服ID")
    private Long agentId;

    @Column(length = 20, comment = "会话状态: OPEN/CLOSED")
    private String status;
}

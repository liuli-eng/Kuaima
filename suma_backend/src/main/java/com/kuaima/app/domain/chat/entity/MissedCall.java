package com.kuaima.app.domain.chat.entity;

import java.sql.Timestamp;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 未接来电记录：BOSS/USER 拨打后对方未接听，写入收件箱式记录，前端轮询拉取。
 */
@Entity
@Table(name = "chat_missed_call")
@Getter
@Setter
public class MissedCall extends BaseEntity {

    @Column(comment = "拨打者用户ID")
    private Long fromUserId;

    @Column(comment = "接收者用户ID")
    private Long toUserId;

    @Column(comment = "通话时间")
    private Timestamp callTime;

    @Column(comment = "通话时长(秒)")
    private Integer duration;

    @Column(comment = "是否已读: false-未读 true-已读")
    private Boolean isRead;
}

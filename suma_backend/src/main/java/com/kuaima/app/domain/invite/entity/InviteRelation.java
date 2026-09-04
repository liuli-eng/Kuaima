package com.kuaima.app.domain.invite.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 邀请关系：记录邀请人(inviterId) 与 被邀请人(inviteeId) 的绑定关系及奖励发放状态。
 */
@Entity
@Table(name = "invite_relation")
@Getter
@Setter
public class InviteRelation extends BaseEntity {

    @Column(comment = "邀请人ID")
    private Long inviterId;

    @Column(comment = "被邀请人ID")
    private Long inviteeId;

    @Column(length = 20, comment = "奖励状态:待发放(PENDING)/已发放(SENT)/无(NONE)")
    private String rewardStatus;
}

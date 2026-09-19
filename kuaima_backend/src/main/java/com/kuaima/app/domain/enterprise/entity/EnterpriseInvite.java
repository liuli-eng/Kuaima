package com.kuaima.app.domain.enterprise.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 企业邀请记录：管理员通过手机号/链接/二维码发出的入企邀请。 */
@Entity
@Table(name = "enterprise_invite", indexes = {
        @Index(name = "idx_ei_enterprise_status", columnList = "enterprise_id,status"),
        @Index(name = "idx_ei_inviter", columnList = "inviter_id")
})
@Getter
@Setter
public class EnterpriseInvite extends BaseEntity {

    @Column(name = "enterprise_id", nullable = false)
    private Long enterpriseId;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 50)
    private String name;

    @Column(nullable = false, length = 30)
    private String inviteRole = "STAFF";

    @Column(length = 40, unique = true)
    private String inviteCode;

    /** PENDING 待接受 / ACCEPTED 已接受 / EXPIRED 已失效 */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "accepted_at")
    private java.sql.Timestamp acceptedAt;
}

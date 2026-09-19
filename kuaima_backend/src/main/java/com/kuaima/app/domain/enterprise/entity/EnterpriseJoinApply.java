package com.kuaima.app.domain.enterprise.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 入企申请：成员通过邀请链接/二维码/手机号邀请后提交的加入申请。 */
@Entity
@Table(name = "enterprise_join_apply", indexes = {
        @Index(name = "idx_eja_enterprise_status", columnList = "enterprise_id,status"),
        @Index(name = "idx_eja_user", columnList = "user_id")
})
@Getter
@Setter
public class EnterpriseJoinApply extends BaseEntity {

    @Column(name = "enterprise_id", nullable = false)
    private Long enterpriseId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 500)
    private String note;

    @Column(name = "apply_role", nullable = false, length = 30)
    private String applyRole = "STAFF";

    /** PENDING 待处理 / AGREED 已同意 / REFUSED 已拒绝 */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 50)
    private String source = "成员邀请链接";

    @Column(name = "handled_by")
    private Long handledBy;
}

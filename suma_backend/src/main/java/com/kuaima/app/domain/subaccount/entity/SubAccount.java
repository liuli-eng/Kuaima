package com.kuaima.app.domain.subaccount.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 子账号关联：BOSS 主账号(parentId) 与子账号(userId) 的绑定关系。
 * 创建子账号时同时创建 sys_user 记录并设置 parentUserId。
 */
@Entity
@Table(name = "sub_account")
@Getter
@Setter
public class SubAccount extends BaseEntity {

    @Column(comment = "主账号ID")
    private Long parentId;

    @Column(comment = "子账号用户ID")
    private Long userId;

    @Column(length = 20, comment = "子账号角色:如 ADMIN/FINANCE/OPERATOR")
    private String role;

    @Column(length = 20, comment = "状态:启用(ACTIVE)/禁用(DISABLED)")
    private String status;
}

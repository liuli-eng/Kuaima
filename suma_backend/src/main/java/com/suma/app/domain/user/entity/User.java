package com.suma.app.domain.user.entity;


import com.alibaba.fastjson2.annotation.JSONField;
import com.suma.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_user")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    @JSONField(serialize = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String role;

    @Column
    private String openid;

    @Column
    private String nickname;

    @Column
    private String avatar;

    @Column
    private String phone;

    @Column
    private String email;

    @Column
    private String status;

    @Column
    private String remark;

    @Column(comment = "年龄")
    private Integer age;

    @Column(comment = "性别")
    private String gender;

    @Transient
    private Integer balance;

    // ============ admin 管理扩展字段 ============

    /** 认证状态: 未认证/待审核/已通过/已拒绝 */
    @Column(length = 20)
    private String certStatus;

    /** 信用分 0~100 */
    @Column
    private Integer creditScore;

    /** 认证类型: REALNAME(零工实名) / ENTERPRISE(企业认证) */
    @Column(length = 30)
    private String certType;

    /** 技能标签（逗号分隔） */
    @Column(length = 200)
    private String skills;

    /** 企业名称（仅 BOSS 用户） */
    @Column(length = 100)
    private String companyName;

    /** 行业类型（仅 BOSS） */
    @Column(length = 50)
    private String industry;

    /** 联系人（仅 BOSS） */
    @Column(length = 50)
    private String contact;

    /** 联系人电话（仅 BOSS） */
    @Column(length = 20)
    private String contactPhone;
}

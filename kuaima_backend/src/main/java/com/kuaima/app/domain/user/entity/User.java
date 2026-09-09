package com.kuaima.app.domain.user.entity;


import java.time.LocalDate;

import com.alibaba.fastjson2.annotation.JSONField;
import com.kuaima.app.domain.base.entity.BaseEntity;

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

    /** 个人实名认证状态：UNVERIFIED/PENDING/APPROVED/REJECTED。 */
    @Column(length = 20)
    private String realnameStatus = "UNVERIFIED";

    /** 企业认证状态：UNVERIFIED/PENDING/APPROVED/REJECTED。 */
    @Column(length = 20)
    private String enterpriseStatus = "UNVERIFIED";

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

    /** 身份证号（实名认证） */
    @Column(length = 50)
    private String idCard;

    /** 真实姓名（实名认证） */
    @Column(length = 50)
    private String realName;

    /** 营业执照号（企业认证） */
    @Column(length = 50)
    private String licenseNo;

    /** 法人代表（企业认证） */
    @Column(length = 50)
    private String legalRep;

    /** 父账号ID（子账号管理） */
    @Column
    private Long parentUserId;

    /** 子账号角色 */
    @Column(length = 20)
    private String subRole;

    /** 城市 */
    @Column(length = 50)
    private String city;

    /** 出生日期（零工个人资料） */
    @Column
    private LocalDate birthday;

    /** 工作年限（零工个人资料） */
    @Column
    private Integer workYears;

    /** 是否接受夜班（零工个人资料） */
    @Column
    private Boolean acceptNightShift;

    /** 个人简介（零工个人资料） */
    @Column(length = 500)
    private String introduction;
}

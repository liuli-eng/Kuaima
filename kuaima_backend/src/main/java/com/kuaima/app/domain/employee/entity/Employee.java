package com.kuaima.app.domain.employee.entity;

import java.util.Date;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 企业员工（后台员工管理）。 */
@Entity
@Table(name = "employee", indexes = {
        @Index(name = "idx_emp_company", columnList = "company"),
        @Index(name = "idx_emp_role", columnList = "role"),
        @Index(name = "idx_emp_status", columnList = "status")
})
@Setter
@Getter
public class Employee extends BaseEntity {

    @Column(comment = "姓名")
    private String name;

    @Column(comment = "手机号")
    private String phone;

    @Column(comment = "职位，如 总经理、人事主管")
    private String job;

    @Column(comment = "所属公司")
    private String company;

    @Column(comment = "角色编码:super超级管理员/admin管理员/staff员工")
    private String role = "staff";

    @Column(comment = "角色名称，如 超级管理员")
    private String roleName;

    @Column(comment = "关联 employee_role.id")
    private Long roleId;

    @Column(comment = "状态:active在职/frozen已停用")
    private String status = "active";

    @Column(columnDefinition = "TEXT", comment = "权限树 JSON")
    private String permissions;

    @Column(comment = "头像底色标识")
    private String avatarColor;

    @Column(comment = "添加时间")
    private Date addTime;
}

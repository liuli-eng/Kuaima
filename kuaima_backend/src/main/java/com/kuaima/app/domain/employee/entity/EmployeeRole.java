package com.kuaima.app.domain.employee.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 员工角色（后台角色管理，含权限树）。 */
@Entity
@Table(name = "employee_role")
@Setter
@Getter
public class EmployeeRole extends BaseEntity {

    @Column(nullable = false, comment = "角色名称")
    private String name;

    @Column(comment = "角色描述")
    private String description;

    @Column(name = "`system`", comment = "是否系统内置角色（不可删）")
    private Boolean system = false;

    @Column(comment = "角色标识色")
    private String color;

    @Column(comment = "角色图标")
    private String icon;

    @Column(columnDefinition = "TEXT", comment = "权限 JSON")
    private String permissions;
}

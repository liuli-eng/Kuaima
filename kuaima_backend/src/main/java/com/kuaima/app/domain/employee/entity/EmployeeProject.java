package com.kuaima.app.domain.employee.entity;

import java.time.LocalDate;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** 员工参与项目（员工-项目多对多关联）。 */
@Entity
@Table(name = "employee_project", uniqueConstraints = {
        @UniqueConstraint(name = "uk_emp_proj", columnNames = {"employee_id", "project_id"})
}, indexes = {
        @Index(name = "idx_ep_project", columnList = "project_id")
})
@Setter
@Getter
public class EmployeeProject extends BaseEntity {

    @Column(comment = "关联 employee.id")
    private Long employeeId;

    @Column(comment = "关联 project.id")
    private Long projectId;

    @Column(comment = "项目内角色，如 项目负责人")
    private String roleInProject;

    @Column(comment = "加入项目日期")
    private LocalDate joinedDate;

    @Column(comment = "状态:active进行中/left已退出")
    private String status = "active";
}

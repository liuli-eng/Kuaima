package com.kuaima.app.admin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 管理员操作日志 */
@Entity
@Table(name = "admin_log")
@Getter @Setter
public class AdminLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String operator;

    @Column(length = 30)
    private String type;       // 登录 / 数据修改 / 审核 / 权限变更 / 删除 / 系统操作

    @Column(length = 100)
    private String target;

    @Column(length = 50)
    private String ip;

    @Column(length = 20)
    private String result;     // 成功 / 失败

    @Column(length = 500)
    private String detail;

    @Column
    private LocalDateTime createTime;
}

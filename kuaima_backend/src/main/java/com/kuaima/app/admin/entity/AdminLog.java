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

    /** 操作人管理员 ID，用于非超管/管理员按本人过滤日志 */
    @Column
    private Long operatorId;

    @Column(length = 30)
    private String type;       // 登录 / 新建 / 编辑 / 删除 / 审核 / 启用 / 禁用

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

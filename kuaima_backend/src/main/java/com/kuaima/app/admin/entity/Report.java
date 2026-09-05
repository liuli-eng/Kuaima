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

/** 举报记录 */
@Entity
@Table(name = "admin_report")
@Getter @Setter
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId;   // 举报人

    @Column(nullable = false)
    private Long targetId;     // 被举报人

    @Column(length = 20)
    private String type;       // 违规 / 纠纷 / 虚假信息

    @Column(length = 500)
    private String reason;

    @Column(length = 20)
    private String status = "待处理"; // 待处理 / 处理中 / 已处理

    @Column(length = 500)
    private String result;     // 处理结果

    @Column
    private Long orderId;      // 关联订单（可选）

    @Column
    private LocalDateTime handleTime;

    @Column
    private LocalDateTime createTime;
}

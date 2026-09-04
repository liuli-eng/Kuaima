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

/**
 * 认证审核记录（User 表已有 certStatus，但保留独立审核表以记录审核轨迹和材料）
 */
@Entity
@Table(name = "admin_certification")
@Getter
@Setter
public class Certification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联用户 ID */
    @Column(nullable = false)
    private Long userId;

    /** 类型：零工实名 / 企业认证 */
    @Column(length = 20)
    private String type;

    @Column(length = 50)
    private String applicantName;

    @Column(length = 50)
    private String contactPhone;

    /** 状态：待审核 / 已通过 / 已拒绝 */
    @Column(length = 20)
    private String status = "待审核";

    @Column(length = 500)
    private String rejectReason;

    @Column
    private LocalDateTime auditTime;

    @Column
    private LocalDateTime applyTime;

    @Column
    private LocalDateTime createTime;
}

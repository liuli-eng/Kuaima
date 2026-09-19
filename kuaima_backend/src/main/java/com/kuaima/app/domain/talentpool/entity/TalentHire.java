package com.kuaima.app.domain.talentpool.entity;

import java.time.LocalDate;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "talent_hire", indexes = {
        @Index(name = "idx_th_boss", columnList = "boss_id"),
        @Index(name = "idx_th_talent", columnList = "talent_id"),
        @Index(name = "idx_th_status", columnList = "status")
})
@Setter
@Getter
public class TalentHire extends BaseEntity {

    @Column(name = "boss_id", nullable = false, columnDefinition = "BIGINT COMMENT '老板用户 id'")
    private Long bossId;

    @Column(name = "talent_id", nullable = false, columnDefinition = "BIGINT COMMENT '关联 talent_pool.id'")
    private Long talentId;

    @Column(name = "worker_id", columnDefinition = "BIGINT COMMENT '关联零工用户 id'")
    private Long workerId;

    @Column(name = "job_id", columnDefinition = "BIGINT COMMENT '关联岗位/订单 id（选填）'")
    private Long jobId;

    @Column(name = "job_name", columnDefinition = "VARCHAR(100) COMMENT '岗位名称快照'")
    private String jobName;

    @Column(name = "work_date", columnDefinition = "DATE COMMENT '工作日期'")
    private LocalDate workDate;

    @Column(columnDefinition = "VARCHAR(500) COMMENT '备注/给零工留言'")
    private String note;

    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'pending' COMMENT '状态:pending待确认/confirmed已确认/finished已完成/cancelled已取消'")
    private String status;
}

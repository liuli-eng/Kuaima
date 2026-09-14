package com.kuaima.app.domain.project.entity;

import java.time.LocalDate;
import java.util.Date;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 考勤打卡记录。 */
@Entity
@Table(name = "attendance_record", indexes = {
        @Index(name = "idx_att_project_date", columnList = "project_id,attend_date"),
        @Index(name = "idx_att_member", columnList = "member_id"),
        @Index(name = "idx_att_status", columnList = "status")
})
@Setter
@Getter
public class AttendanceRecord extends BaseEntity {

    @Column(comment = "关联 project.id")
    private Long projectId;

    @Column(comment = "关联 project_member.id")
    private Long memberId;

    @Column(comment = "关联用户/员工 id")
    private Long userId;

    @Column(comment = "打卡人姓名")
    private String name;

    @Column(comment = "考勤日期")
    private LocalDate attendDate;

    @Column(comment = "签到时间")
    private Date signInTime;

    @Column(comment = "签退时间")
    private Date signOutTime;

    @Column(comment = "状态:on出勤/late迟到/absent缺卡/leave请假")
    private String status = "on";

    @Column(comment = "迟到分钟数")
    private Integer lateMinutes = 0;
}

package com.kuaima.app.domain.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.project.constant.ProjectConstants;
import com.kuaima.app.domain.project.entity.AttendanceRecord;
import com.kuaima.app.domain.project.entity.Project;
import com.kuaima.app.domain.project.entity.ProjectMember;
import com.kuaima.app.domain.project.repository.AttendanceRepository;
import com.kuaima.app.domain.project.repository.ProjectMemberRepository;
import com.kuaima.app.domain.project.repository.ProjectRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
            ProjectMemberRepository memberRepository, ProjectRepository projectRepository) {
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
        this.projectRepository = projectRepository;
    }

    public List<AttendanceRecord> listByDate(Long projectId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return attendanceRepository.findByProjectIdAndAttendDate(projectId, date);
    }

    /** 签到/签退：同一成员同一天只有一条记录，按状态自动判定迟到。 */
    @Transactional
    public AttendanceRecord clock(Long projectId, Long memberId, String name, boolean signOut, Integer lateThreshold) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRepository.findByProjectIdAndAttendDate(projectId, today).stream()
                .filter(r -> memberId != null ? memberId.equals(r.getMemberId())
                        : name != null && name.equals(r.getName()))
                .findFirst()
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new AttendanceRecord();
            record.setProjectId(projectId);
            record.setMemberId(memberId);
            record.setName(name);
            record.setAttendDate(today);
            record.setSignInTime(java.util.Date.from(now.atZone(java.time.ZoneId.systemDefault()).toInstant()));
            int threshold = lateThreshold != null ? lateThreshold
                    : projectRepository.findById(projectId).map(Project::getLateThreshold).orElse(10);
            boolean late = now.toLocalTime().isAfter(LocalTime.of(8, 0).plusMinutes(threshold));
            record.setStatus(late ? ProjectConstants.ATTEND_LATE : ProjectConstants.ATTEND_ON);
            record.setLateMinutes(late
                    ? (int) java.time.Duration.between(LocalTime.of(8, 0).plusMinutes(threshold), now.toLocalTime()).toMinutes()
                    : 0);
        } else if (signOut) {
            record.setSignOutTime(java.util.Date.from(now.atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        return attendanceRepository.save(record);
    }

    /** 当日考勤统计：应到 / 实到 / 迟到 / 缺卡 / 请假。 */
    public Map<String, Object> statsForDate(Long projectId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<ProjectMember> members = new ArrayList<>();
        members.addAll(memberRepository.findByProjectIdAndStatus(projectId, ProjectConstants.MEMBER_ACTIVE));
        members.addAll(memberRepository.findByProjectIdAndStatus(projectId, ProjectConstants.MEMBER_TEMP));

        List<AttendanceRecord> records = attendanceRepository.findByProjectIdAndAttendDate(projectId, date);
        long on = records.stream().filter(r -> ProjectConstants.ATTEND_ON.equals(r.getStatus())).count();
        long late = records.stream().filter(r -> ProjectConstants.ATTEND_LATE.equals(r.getStatus())).count();
        long leave = records.stream().filter(r -> ProjectConstants.ATTEND_LEAVE.equals(r.getStatus())).count();
        long present = records.size();
        long shouldArrive = members.size();
        long absent = shouldArrive - present > 0 ? shouldArrive - present : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date.toString());
        result.put("shouldArrive", shouldArrive);
        result.put("actual", on + late);
        result.put("on", on);
        result.put("late", late);
        result.put("absent", absent);
        result.put("leave", leave);
        return result;
    }

    /** 区间签到汇总（按天聚合），用于签到记录页。 */
    public List<Map<String, Object>> dailySummary(Long projectId, LocalDate start, LocalDate end) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (start == null) {
            start = LocalDate.now().minusDays(6);
        }
        if (end == null) {
            end = LocalDate.now();
        }
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            List<AttendanceRecord> records = attendanceRepository.findByProjectIdAndAttendDate(projectId, d);
            long on = records.stream().filter(r -> ProjectConstants.ATTEND_ON.equals(r.getStatus())).count();
            long late = records.stream().filter(r -> ProjectConstants.ATTEND_LATE.equals(r.getStatus())).count();
            long absent = records.stream().filter(r -> ProjectConstants.ATTEND_ABSENT.equals(r.getStatus())).count();
            long leave = records.stream().filter(r -> ProjectConstants.ATTEND_LEAVE.equals(r.getStatus())).count();
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", d.toString());
            day.put("actual", on + late);
            day.put("late", late);
            day.put("absent", absent);
            day.put("leave", leave);
            list.add(day);
        }
        return list;
    }

    public AttendanceRecord getOrThrow(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("考勤记录不存在: " + id));
    }
}

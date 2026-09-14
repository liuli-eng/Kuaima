package com.kuaima.app.controller.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.project.entity.AttendanceRecord;
import com.kuaima.app.domain.project.service.AttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/projects/{projectId}")
@Tag(name = "老板-考勤打卡", description = "考勤列表、当日统计、签到/签退、签到记录")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/attendance")
    @Operation(summary = "当日/指定日期考勤列表")
    public Result<List<AttendanceRecord>> list(@PathVariable Long projectId,
            @RequestParam(required = false) LocalDate date) {
        return Result.success(attendanceService.listByDate(projectId, date));
    }

    @GetMapping("/attendance/stats")
    @Operation(summary = "当日考勤统计：应到/实到/迟到/缺卡/请假")
    public Result<Map<String, Object>> stats(@PathVariable Long projectId,
            @RequestParam(required = false) LocalDate date) {
        return Result.success(attendanceService.statsForDate(projectId, date));
    }

    @PostMapping("/attendance/clock")
    @Operation(summary = "签到/签退（signOut=true 为签退）")
    public Result<AttendanceRecord> clock(@PathVariable Long projectId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "false") boolean signOut) {
        return Result.success(attendanceService.clock(projectId, memberId, name, signOut, null));
    }

    @GetMapping("/checkin")
    @Operation(summary = "签到记录（按天聚合，支持区间）")
    public Result<List<Map<String, Object>>> checkin(@PathVariable Long projectId,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        return Result.success(attendanceService.dailySummary(projectId, start, end));
    }
}

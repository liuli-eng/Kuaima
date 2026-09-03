package com.suma.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.admin.entity.Report;
import com.suma.app.admin.repository.ReportRepository;
import com.suma.app.common.Result;

/** 举报处理 */
@RestController
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportRepository repo;

    public ReportController(ReportRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<List<Report>> list() { return Result.success(repo.findAll()); }

    @GetMapping("/{id}")
    public Result<Report> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    /** 创建举报（来自用户端） */
    @PostMapping
    public Result<Report> create(@RequestBody Report report) {
        report.setCreateTime(LocalDateTime.now());
        return Result.success(repo.save(report));
    }

    /** 处理举报 */
    @PostMapping("/{id}/handle")
    public Result<Report> handle(@PathVariable Long id, @RequestParam String result) {
        Report r = repo.findById(id).orElseThrow();
        r.setStatus("已处理");
        r.setResult(result);
        r.setHandleTime(LocalDateTime.now());
        return Result.success(repo.save(r));
    }
}

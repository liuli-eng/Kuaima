package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Report;
import com.kuaima.app.admin.repository.ReportRepository;
import com.kuaima.app.common.Result;

/** 举报处理 */
@RestController
@RequestMapping("/admin/reports")
@Tag(name = "后台-举报", description = "用户举报处理")
public class ReportController {

    private final ReportRepository repo;

    public ReportController(ReportRepository repo) { this.repo = repo; }

    @Operation(summary = "举报列表", description = "返回全部举报记录，无分页")
    @GetMapping
    public Result<List<Report>> list() { return Result.success(repo.findAll()); }

    @Operation(summary = "举报详情", description = "按 id 查询举报记录；不存在抛出异常")
    @GetMapping("/{id}")
    public Result<Report> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    /** 创建举报（来自用户端） */
    @Operation(summary = "创建举报", description = "body 为 Report 字段；自动填充 createTime 为当前时间")
    @PostMapping
    public Result<Report> create(@RequestBody Report report) {
        report.setCreateTime(LocalDateTime.now());
        return Result.success(repo.save(report));
    }

    /** 处理举报 */
    @Operation(summary = "处理举报", description = "将举报置为已处理，记录处理结果 result(必填)与处理时间 handleTime；举报不存在抛出异常")
    @PostMapping("/{id}/handle")
    public Result<Report> handle(@PathVariable Long id, @RequestParam String result) {
        Report r = repo.findById(id).orElseThrow();
        r.setStatus("已处理");
        r.setResult(result);
        r.setHandleTime(LocalDateTime.now());
        return Result.success(repo.save(r));
    }
}

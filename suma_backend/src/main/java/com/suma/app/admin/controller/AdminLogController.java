package com.suma.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.admin.entity.AdminLog;
import com.suma.app.admin.repository.AdminLogRepository;
import com.suma.app.common.Result;

/** 操作日志查询（只读） */
@RestController
@RequestMapping("/admin/logs")
public class AdminLogController {

    private final AdminLogRepository repo;

    public AdminLogController(AdminLogRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<Page<AdminLog>> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<AdminLog> result = repo.findAll(pageable);
        return Result.success(result, page, result.getTotalElements());
    }
}

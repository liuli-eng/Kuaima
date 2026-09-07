package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;
import com.kuaima.app.common.Result;

/** 规则管理 CRUD */
@RestController
@RequestMapping("/admin/rules")
@Tag(name = "后台-规则", description = "平台规则管理")
public class RulesController {

    private final RulesRepository repo;

    public RulesController(RulesRepository repo) { this.repo = repo; }

    /** 列表（分页，按 id 倒序） */
    @Operation(summary = "规则列表分页", description = "按 id 倒序分页返回 Rules 列表")
    @GetMapping
    public Result<List<Rules>> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Rules> result = repo.findAll(pageable);
        return Result.success(result.getContent(), page, result.getTotalElements());
    }

    @Operation(summary = "规则详情", description = "按 id 查询 Rules 完整信息")
    @GetMapping("/{id}")
    public Result<Rules> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    @Operation(summary = "新增规则", description = "创建 Rules，category 可选 通知公告/信用评定/收费标准/交易规则/隐私协议，version 默认 v1.0")
    @PostMapping
    public Result<Rules> create(@RequestBody Rules rules) {
        rules.setCreateTime(LocalDateTime.now());
        rules.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(rules));
    }

    @Operation(summary = "更新规则", description = "按 id 更新 Rules，字段非空才更新")
    @PutMapping("/{id}")
    public Result<Rules> update(@PathVariable Long id, @RequestBody Rules rules) {
        Rules existing = repo.findById(id).orElseThrow();
        if (rules.getTitle() != null) existing.setTitle(rules.getTitle());
        if (rules.getCategory() != null) existing.setCategory(rules.getCategory());
        if (rules.getVersion() != null) existing.setVersion(rules.getVersion());
        if (rules.getStatus() != null) existing.setStatus(rules.getStatus());
        if (rules.getEffectiveTime() != null) existing.setEffectiveTime(rules.getEffectiveTime());
        if (rules.getContent() != null) existing.setContent(rules.getContent());
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    @Operation(summary = "删除规则", description = "按 id 删除 Rules")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}

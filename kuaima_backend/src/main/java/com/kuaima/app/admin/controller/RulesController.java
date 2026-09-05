package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;
import com.kuaima.app.common.Result;

/** 规则管理 CRUD */
@RestController
@RequestMapping("/admin/rules")
public class RulesController {

    private final RulesRepository repo;

    public RulesController(RulesRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<List<Rules>> list() { return Result.success(repo.findAll()); }

    @GetMapping("/{id}")
    public Result<Rules> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    @PostMapping
    public Result<Rules> create(@RequestBody Rules rules) {
        rules.setCreateTime(LocalDateTime.now());
        rules.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(rules));
    }

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

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}

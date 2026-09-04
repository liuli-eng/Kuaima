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

import com.kuaima.app.admin.entity.Blacklist;
import com.kuaima.app.admin.repository.BlacklistRepository;
import com.kuaima.app.common.Result;

/** 黑名单管理 */
@RestController
@RequestMapping("/admin/blacklists")
public class BlacklistController {

    private final BlacklistRepository repo;

    public BlacklistController(BlacklistRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<List<Blacklist>> list() { return Result.success(repo.findAll()); }

    @GetMapping("/{id}")
    public Result<Blacklist> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    /** 加入黑名单 */
    @PostMapping
    public Result<Blacklist> create(@RequestBody Blacklist blacklist) {
        blacklist.setCreateTime(LocalDateTime.now());
        blacklist.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(blacklist));
    }

    /** 解封 */
    @PutMapping("/{id}/unfreeze")
    public Result<Blacklist> unfreeze(@PathVariable Long id) {
        Blacklist b = repo.findById(id).orElseThrow();
        b.setStatus("已解封");
        b.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(b));
    }

    /** 延长封禁 */
    @PutMapping("/{id}/extend")
    public Result<Blacklist> extend(@PathVariable Long id, @RequestBody Blacklist patch) {
        Blacklist b = repo.findById(id).orElseThrow();
        if (patch.getExpireTime() != null) b.setExpireTime(patch.getExpireTime());
        b.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(b));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}

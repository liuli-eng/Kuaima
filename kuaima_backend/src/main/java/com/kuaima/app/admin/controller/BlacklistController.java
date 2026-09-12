package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "后台-黑名单", description = "用户黑名单管理")
public class BlacklistController {

    private final BlacklistRepository repo;

    public BlacklistController(BlacklistRepository repo) { this.repo = repo; }

    @Operation(summary = "黑名单列表", description = "返回全部黑名单记录，无分页")
    @GetMapping
    public Result<List<Blacklist>> list() { return Result.success(repo.findAll()); }

    @Operation(summary = "黑名单详情", description = "按 id 查询记录；不存在抛出异常")
    @GetMapping("/{id}")
    public Result<Blacklist> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    /** 加入黑名单 */
    @Operation(summary = "加入黑名单", description = "body 为 Blacklist 字段；自动填充 createTime/updateTime 为当前时间")
    @PostMapping
    public Result<Blacklist> create(@RequestBody Blacklist blacklist) {
        blacklist.setCreateTime(LocalDateTime.now());
        blacklist.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(blacklist));
    }

    /** 解封 */
    @Operation(summary = "解封用户", description = "将记录置为已解封并刷新 updateTime；记录不存在抛出异常")
    @PutMapping("/{id}/unfreeze")
    public Result<Blacklist> unfreeze(@PathVariable Long id) {
        Blacklist b = repo.findById(id).orElseThrow();
        b.setStatus("已解封");
        b.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(b));
    }

    /** 延长封禁 */
    @Operation(summary = "延长封禁", description = "body 携带 expireTime 时覆盖过期时间，并刷新 updateTime；记录不存在抛出异常")
    @PutMapping("/{id}/extend")
    public Result<Blacklist> extend(@PathVariable Long id, @RequestBody Blacklist patch) {
        Blacklist b = repo.findById(id).orElseThrow();
        if (patch.getExpireTime() != null) b.setExpireTime(patch.getExpireTime());
        b.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(b));
    }

    @Operation(summary = "删除黑名单记录", description = "按 id 删除黑名单记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}

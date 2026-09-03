package com.suma.app.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.admin.entity.AdminSetting;
import com.suma.app.admin.repository.AdminSettingRepository;
import com.suma.app.common.Result;

import java.time.LocalDateTime;

/** 系统设置 */
@RestController
@RequestMapping("/admin/settings")
public class AdminSettingController {

    private final AdminSettingRepository repo;

    public AdminSettingController(AdminSettingRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<List<AdminSetting>> list() { return Result.success(repo.findAll()); }

    @GetMapping("/category/{category}")
    public Result<List<AdminSetting>> byCategory(@PathVariable String category) {
        return Result.success(repo.findByCategory(category));
    }

    @GetMapping("/{key}")
    public Result<AdminSetting> get(@PathVariable String key) {
        return Result.success(repo.findById(key).orElseThrow());
    }

    /** 保存/更新 设置 */
    @PutMapping("/{key}")
    public Result<AdminSetting> save(@PathVariable String key, @RequestBody AdminSetting setting) {
        setting.setSettingKey(key);
        return Result.success(repo.save(setting));
    }
}

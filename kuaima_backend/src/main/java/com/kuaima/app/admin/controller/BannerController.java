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

import com.kuaima.app.admin.entity.Banner;
import com.kuaima.app.admin.repository.BannerRepository;
import com.kuaima.app.common.Result;

/** Banner 管理 CRUD */
@RestController
@RequestMapping("/admin/banners")
public class BannerController {

    private final BannerRepository repo;

    public BannerController(BannerRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<List<Banner>> list() { return Result.success(repo.findAll()); }

    @GetMapping("/{id}")
    public Result<Banner> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    @PostMapping
    public Result<Banner> create(@RequestBody Banner banner) {
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(banner));
    }

    @PutMapping("/{id}")
    public Result<Banner> update(@PathVariable Long id, @RequestBody Banner banner) {
        Banner existing = repo.findById(id).orElseThrow();
        existing.setTitle(banner.getTitle() != null ? banner.getTitle() : existing.getTitle());
        existing.setImageUrl(banner.getImageUrl() != null ? banner.getImageUrl() : existing.getImageUrl());
        existing.setPosition(banner.getPosition() != null ? banner.getPosition() : existing.getPosition());
        existing.setWeight(banner.getWeight() != null ? banner.getWeight() : existing.getWeight());
        existing.setLinkUrl(banner.getLinkUrl() != null ? banner.getLinkUrl() : existing.getLinkUrl());
        existing.setStatus(banner.getStatus() != null ? banner.getStatus() : existing.getStatus());
        existing.setStartTime(banner.getStartTime() != null ? banner.getStartTime() : existing.getStartTime());
        existing.setEndTime(banner.getEndTime() != null ? banner.getEndTime() : existing.getEndTime());
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(repo.save(existing));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return Result.success();
    }
}

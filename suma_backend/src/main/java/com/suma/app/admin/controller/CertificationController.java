package com.suma.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.admin.entity.Certification;
import com.suma.app.admin.repository.CertificationRepository;
import com.suma.app.common.Result;

/** 认证审核 */
@RestController
@RequestMapping("/admin/certifications")
public class CertificationController {

    private final CertificationRepository repo;

    public CertificationController(CertificationRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<List<Certification>> list() { return Result.success(repo.findAll()); }

    @GetMapping("/{id}")
    public Result<Certification> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    /** 审核通过 */
    @PutMapping("/{id}/pass")
    public Result<Certification> pass(@PathVariable Long id) {
        Certification c = repo.findById(id).orElseThrow();
        c.setStatus("已通过");
        c.setAuditTime(LocalDateTime.now());
        return Result.success(repo.save(c));
    }

    /** 审核拒绝 */
    @PutMapping("/{id}/reject")
    public Result<Certification> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        Certification c = repo.findById(id).orElseThrow();
        c.setStatus("已拒绝");
        c.setRejectReason(reason);
        c.setAuditTime(LocalDateTime.now());
        return Result.success(repo.save(c));
    }
}

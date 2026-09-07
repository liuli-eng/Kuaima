package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Certification;
import com.kuaima.app.admin.repository.CertificationRepository;
import com.kuaima.app.common.Result;

/** 认证审核 */
@RestController
@RequestMapping("/admin/certifications")
public class CertificationController {

    private final CertificationRepository repo;

    public CertificationController(CertificationRepository repo) { this.repo = repo; }

    /** 列表（分页，按 type/status 过滤，按 id 倒序） */
    @GetMapping
    public Result<List<Certification>> list(@RequestParam(required = false) String type,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Certification> result;
        boolean hasType = type != null && !type.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();
        if (hasType && hasStatus) {
            result = repo.findByTypeAndStatus(type, status, pageable);
        } else if (hasType) {
            result = repo.findByType(type, pageable);
        } else if (hasStatus) {
            result = repo.findByStatus(status, pageable);
        } else {
            result = repo.findAll(pageable);
        }
        return Result.success(result.getContent(), page, result.getTotalElements());
    }

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

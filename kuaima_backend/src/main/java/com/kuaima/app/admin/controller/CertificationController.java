package com.kuaima.app.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import com.kuaima.app.domain.user.service.CertificationService;

/** 认证审核 */
@RestController
@RequestMapping("/admin/certifications")
@Tag(name = "后台-认证审核", description = "身份认证审核记录")
public class CertificationController {

    private final CertificationRepository repo;
    private final CertificationService certificationService;

    public CertificationController(CertificationRepository repo, CertificationService certificationService) {
        this.repo = repo;
        this.certificationService = certificationService;
    }

    /** 列表（分页，按 type/status 过滤，按 id 倒序） */
    @Operation(summary = "认证审核列表", description = "按 type(零工实名/企业认证) 和 status(待审核/已通过/已拒绝) 过滤分页，按 id 倒序")
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

    @Operation(summary = "认证详情", description = "按 id 查询 Certification 完整信息")
    @GetMapping("/{id}")
    public Result<Certification> get(@PathVariable Long id) {
        return Result.success(repo.findById(id).orElseThrow());
    }

    /** 审核通过 */
    @Operation(summary = "认证审核通过", description = "将 status 置为「已通过」，记录 auditTime")
    @PutMapping("/{id}/pass")
    public Result<Certification> pass(@PathVariable Long id) {
        return Result.success(certificationService.audit(id, true, null));
    }

    /** 审核拒绝 */
    @Operation(summary = "认证审核拒绝", description = "将 status 置为「已拒绝」，记录 rejectReason 和 auditTime")
    @PutMapping("/{id}/reject")
    public Result<Certification> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return Result.success(certificationService.audit(id, false, reason));
    }
}

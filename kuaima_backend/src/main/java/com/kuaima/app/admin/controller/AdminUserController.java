package com.kuaima.app.admin.controller;

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

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.user.constant.CertificationStatus;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 后台用户管理（零工列表 / 雇主列表 / 冻结解冻）
 */
@RestController
@RequestMapping("/admin/users")
@Tag(name = "后台-用户", description = "用户列表与统计")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 零工列表 */
    @Operation(summary = "零工列表分页", description = "参数：status(正常/冻结)、keyword(昵称/手机号模糊)、page(默认 0)、size(默认 10)。返回 User，不含 password")
    @GetMapping("/workers")
    public Result<Page<User>> workers(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String kw = keyword != null && !keyword.isBlank() ? keyword : null;
        Page<User> result = userRepository.searchByRole(UserRole.USER, status, kw, pageable);
        return Result.success(result, page, result.getTotalElements());
    }

    /** 雇主列表 */
    @Operation(summary = "雇主列表分页", description = "参数：status(正常/冻结)、enterpriseStatus(UNVERIFIED/PENDING/APPROVED/REJECTED)、keyword(企业名/手机号模糊)、page、size")
    @GetMapping("/bosses")
    public Result<Page<User>> bosses(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String enterpriseStatus,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String kw = keyword != null && !keyword.isBlank() ? keyword : null;
        String es = enterpriseStatus != null && !enterpriseStatus.isBlank() ? enterpriseStatus : null;
        Page<User> result = userRepository.searchBosses(UserRole.BOSS, status, es, kw, pageable);
        return Result.success(result, page, result.getTotalElements());
    }

    /** 用户详情 */
    @Operation(summary = "用户详情", description = "按 id 查询用户完整信息，用户不存在返回 404")
    @GetMapping("/{id}")
    public Result<User> get(@PathVariable Long id) {
        return Result.success(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id)));
    }

    /** 冻结 */
    @Operation(summary = "冻结用户", description = "将用户 status 置为「冻结」")
    @PutMapping("/{id}/freeze")
    public Result<User> freeze(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
        u.setStatus("冻结");
        return Result.success(userRepository.save(u));
    }

    /** 解冻 */
    @Operation(summary = "解冻用户", description = "将用户 status 置为「正常」")
    @PutMapping("/{id}/unfreeze")
    public Result<User> unfreeze(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
        u.setStatus("正常");
        return Result.success(userRepository.save(u));
    }

    /** 批量冻结 */
    @Operation(summary = "批量冻结用户", description = "按 ids 数组批量将用户 status 置为「冻结」")
    @PutMapping("/freeze/batch")
    public Result<Void> freezeBatch(@RequestParam java.util.List<Long> ids) {
        for (Long id : ids) {
            userRepository.findById(id).ifPresent(u -> {
                u.setStatus("冻结");
                userRepository.save(u);
            });
        }
        return Result.success();
    }

    /** 批量解冻 */
    @Operation(summary = "批量解冻用户", description = "按 ids 数组批量将用户 status 置为「正常」")
    @PutMapping("/unfreeze/batch")
    public Result<Void> unfreezeBatch(@RequestParam java.util.List<Long> ids) {
        for (Long id : ids) {
            userRepository.findById(id).ifPresent(u -> {
                u.setStatus("正常");
                userRepository.save(u);
            });
        }
        return Result.success();
    }

    /** 企业认证审核通过 */
    @Operation(summary = "企业认证审核通过", description = "将雇主 enterpriseStatus 置为 APPROVED、certStatus 置为「已通过」")
    @PutMapping("/{id}/enterprise/pass")
    public Result<User> enterprisePass(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
        u.setEnterpriseStatus(CertificationStatus.APPROVED);
        u.setCertStatus("已通过");
        u.setCertType("ENTERPRISE");
        return Result.success(userRepository.save(u));
    }

    /** 企业认证审核拒绝 */
    @Operation(summary = "企业认证审核拒绝", description = "将雇主 enterpriseStatus 置为 REJECTED、certStatus 置为「已拒绝」")
    @PutMapping("/{id}/enterprise/reject")
    public Result<User> enterpriseReject(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
        u.setEnterpriseStatus(CertificationStatus.REJECTED);
        u.setCertStatus("已拒绝");
        u.setCertType("ENTERPRISE");
        return Result.success(userRepository.save(u));
    }
}

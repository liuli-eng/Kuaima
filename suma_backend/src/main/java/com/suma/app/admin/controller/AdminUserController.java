package com.suma.app.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suma.app.common.Result;
import com.suma.app.domain.user.constant.UserRole;
import com.suma.app.domain.user.entity.User;
import com.suma.app.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 后台用户管理（零工列表 / 雇主列表 / 冻结解冻）
 */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 零工列表 */
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
    @GetMapping("/bosses")
    public Result<Page<User>> bosses(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String kw = keyword != null && !keyword.isBlank() ? keyword : null;
        Page<User> result = userRepository.searchByRole(UserRole.BOSS, status, kw, pageable);
        return Result.success(result, page, result.getTotalElements());
    }

    /** 用户详情 */
    @GetMapping("/{id}")
    public Result<User> get(@PathVariable Long id) {
        return Result.success(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id)));
    }

    /** 冻结 */
    @PutMapping("/{id}/freeze")
    public Result<User> freeze(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
        u.setStatus("冻结");
        return Result.success(userRepository.save(u));
    }

    /** 解冻 */
    @PutMapping("/{id}/unfreeze")
    public Result<User> unfreeze(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
        u.setStatus("正常");
        return Result.success(userRepository.save(u));
    }

    /** 批量冻结 */
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
}

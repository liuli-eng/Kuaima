package com.kuaima.app.controller.finance;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.subaccount.entity.SubAccount;
import com.kuaima.app.domain.subaccount.repository.SubAccountRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;

/**
 * 老板子账号管理。
 * 创建子账号时自动同步创建底层 sys_user 记录（若 userId 不存在），并设置 parentUserId。
 */
@RestController
@RequestMapping("/boss/sub-accounts")
public class SubAccountController {

    private final SubAccountRepository subAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SubAccountController(SubAccountRepository subAccountRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.subAccountRepository = subAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 子账号列表：GET /boss/sub-accounts?parentId=1 */
    @GetMapping
    public Result<List<SubAccount>> listSubAccounts(@RequestParam Long parentId) {
        return Result.success(subAccountRepository.findByParentId(parentId));
    }

    /**
     * 创建子账号：POST /boss/sub-accounts
     * body: { "parentId": 1, "username": "sub001", "nickname": "子账号", "phone": "138...", "role": "FINANCE" }
     * 若传入 userId 且该用户已存在，则直接绑定；否则新建 sys_user 记录。
     */
    @PostMapping
    @Transactional
    public Result<SubAccount> createSubAccount(@RequestBody Map<String, Object> body) {
        Long parentId = body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : null;
        if (parentId == null) {
            throw new IllegalArgumentException("parentId 不能为空");
        }
        String role = body.get("role") != null ? body.get("role").toString() : "OPERATOR";

        Long userId;
        if (body.get("userId") != null) {
            // 绑定已有用户
            userId = Long.valueOf(body.get("userId").toString());
            if (!userRepository.existsById(userId)) {
                throw new IllegalArgumentException("指定的 userId 不存在: " + userId);
            }
        } else {
            // 新建 sys_user
            String username = body.get("username") != null ? body.get("username").toString() : null;
            if (!StringUtils.hasText(username)) {
                username = "sub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            }
            if (userRepository.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("账号已存在: " + username);
            }
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setRole("BOSS");
            newUser.setNickname(body.get("nickname") != null ? body.get("nickname").toString() : "子账号");
            newUser.setPhone(body.get("phone") != null ? body.get("phone").toString() : null);
            newUser.setParentUserId(parentId);
            newUser.setSubRole(role);
            newUser.setStatus("正常");
            userId = userRepository.save(newUser).getId();
        }

        SubAccount sub = new SubAccount();
        sub.setParentId(parentId);
        sub.setUserId(userId);
        sub.setRole(role);
        sub.setStatus("ACTIVE");
        return Result.success(subAccountRepository.save(sub));
    }

    /** 删除子账号：DELETE /boss/sub-accounts/{id} */
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> deleteSubAccount(@PathVariable Long id) {
        SubAccount sub = subAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("子账号不存在: " + id));
        // 同步禁用底层用户
        userRepository.findById(sub.getUserId()).ifPresent(u -> {
            u.setStatus("冻结");
            userRepository.save(u);
        });
        subAccountRepository.deleteById(id);
        return Result.success();
    }

    /** 修改子账号角色：PUT /boss/sub-accounts/{id}/role?role=FINANCE */
    @PutMapping("/{id}/role")
    @Transactional
    public Result<SubAccount> updateRole(@PathVariable Long id, @RequestParam String role) {
        SubAccount sub = subAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("子账号不存在: " + id));
        sub.setRole(role);
        // 同步更新用户的 subRole
        userRepository.findById(sub.getUserId()).ifPresent(u -> {
            u.setSubRole(role);
            userRepository.save(u);
        });
        return Result.success(subAccountRepository.save(sub));
    }
}

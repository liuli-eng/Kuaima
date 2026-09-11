package com.kuaima.app.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
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
    private final BaseOrderItemRespository orderItemRepository;
    private final BossOrderRespository bossOrderRepository;

    public AdminUserController(UserRepository userRepository,
                               BaseOrderItemRespository orderItemRepository,
                               BossOrderRespository bossOrderRepository) {
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.bossOrderRepository = bossOrderRepository;
    }

    /** 零工列表（附加 completedOrders 已完成订单数） */
    @Operation(summary = "零工列表分页", description = "参数：status(正常/冻结)、keyword(昵称/手机号模糊)、page(默认 0)、size(默认 10)。返回 User，不含 password，附加 completedOrders")
    @GetMapping("/workers")
    public Result<Page<JSONObject>> workers(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String kw = keyword != null && !keyword.isBlank() ? keyword : null;
        Page<User> result = userRepository.searchByRole(UserRole.USER, status, kw, pageable);

        // 批量统计零工已完成订单数
        Set<Long> userIds = result.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, Long> completedMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            orderItemRepository.countCompletedByUserIds(userIds)
                    .forEach(row -> completedMap.put((Long) row[0], (Long) row[1]));
        }

        Page<JSONObject> views = result.map(u -> {
            JSONObject obj = (JSONObject) JSON.toJSON(u);
            obj.put("completedOrders", completedMap.getOrDefault(u.getId(), 0L));
            return obj;
        });
        return Result.success(views, page, result.getTotalElements());
    }

    /** 雇主列表（附加 jobsCount 招工数，使用 fastjson 序列化确保 password 不泄露） */
    @Operation(summary = "雇主列表分页", description = "参数：status(正常/冻结)、enterpriseStatus(UNVERIFIED/PENDING/APPROVED/REJECTED)、keyword(企业名/手机号模糊)、page、size。附加 jobsCount")
    @GetMapping("/bosses")
    public Result<Page<JSONObject>> bosses(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) String enterpriseStatus,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String kw = keyword != null && !keyword.isBlank() ? keyword : null;
        String es = enterpriseStatus != null && !enterpriseStatus.isBlank() ? enterpriseStatus : null;
        Page<User> result = userRepository.searchBosses(UserRole.BOSS, status, es, kw, pageable);

        // 批量统计每个老板的招工数
        Set<Long> bossIds = result.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, Long> jobsMap = new HashMap<>();
        if (!bossIds.isEmpty()) {
            bossOrderRepository.countByCreateByIds(bossIds)
                    .forEach(row -> jobsMap.put((Long) row[0], (Long) row[1]));
        }

        Page<JSONObject> views = result.map(u -> {
            JSONObject obj = (JSONObject) JSON.toJSON(u);
            obj.put("jobsCount", jobsMap.getOrDefault(u.getId(), 0L));
            return obj;
        });
        return Result.success(views, page, result.getTotalElements());
    }

    /** 用户详情 */
    @Operation(summary = "用户详情", description = "按 id 查询用户完整信息，用户不存在返回 404")
    @GetMapping("/{id}")
    public Result<JSONObject> get(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在: " + id));
        JSONObject obj = (JSONObject) JSON.toJSON(u);
        obj.remove("password");
        // 附加完成订单数
        Map<Long, Long> completedMap = new HashMap<>();
        Set<Long> uid = Set.of(id);
        orderItemRepository.countCompletedByUserIds(uid)
                .forEach(row -> completedMap.put((Long) row[0], (Long) row[1]));
        obj.put("completedOrders", completedMap.getOrDefault(id, 0L));
        return Result.success(obj);
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

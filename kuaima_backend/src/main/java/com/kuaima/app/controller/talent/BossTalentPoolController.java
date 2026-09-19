package com.kuaima.app.controller.talent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.talentpool.entity.TalentHire;
import com.kuaima.app.domain.talentpool.entity.TalentPool;
import com.kuaima.app.domain.talentpool.entity.TalentReview;
import com.kuaima.app.domain.talentpool.service.TalentPoolService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/boss/talent-pool")
@Tag(name = "老板-人才库（候选人）", description = "人才库列表、详情、收藏、雇佣")
@RequiredArgsConstructor
public class BossTalentPoolController {

    private final TalentPoolService talentPoolService;

    @Operation(summary = "人才库列表", description = "按关键字（姓名/技能/工种）、类型（skilled熟练工/new新零工）、是否收藏筛选，分页返回")
    @GetMapping
    public Result<List<TalentPool>> listTalents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean favoriteOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long bossId = requireBossId(authentication);
        Page<TalentPool> result = talentPoolService.listTalents(bossId, keyword, type, favoriteOnly, page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    @Operation(summary = "人才详情", description = "返回人才信息、雇主评价列表与历史雇佣记录")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getTalentDetail(@PathVariable Long id, Authentication authentication) {
        Long bossId = requireBossId(authentication);
        return Result.success(talentPoolService.getTalentDetail(bossId, id));
    }

    @Operation(summary = "添加零工到人才库", description = "手动添加零工条目到当前老板的人才库")
    @PostMapping
    public Result<TalentPool> addTalent(@RequestBody TalentPool talent, Authentication authentication) {
        Long bossId = requireBossId(authentication);
        return Result.success(talentPoolService.addToPool(bossId, talent));
    }

    @Operation(summary = "收藏/取消收藏", description = "body 可选 {favorite: true/false}，缺省则切换当前状态")
    @PutMapping("/{id}/favorite")
    public Result<Map<String, Object>> toggleFavorite(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            Authentication authentication) {
        Long bossId = requireBossId(authentication);
        Boolean favorite = body == null || body.get("favorite") == null
                ? null : Boolean.valueOf(body.get("favorite").toString());
        return Result.success(talentPoolService.toggleFavorite(bossId, id, favorite));
    }

    @Operation(summary = "雇佣零工", description = "body：{jobId(选填), jobName(选填), workDate(yyyy-MM-dd), note(选填)}；创建雇佣记录并向零工发送站内通知")
    @PostMapping("/{id}/hire")
    public Result<TalentHire> hireTalent(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            Authentication authentication) {
        Long bossId = requireBossId(authentication);
        Long jobId = toLong(body == null ? null : body.get("jobId"), "jobId");
        String jobName = body == null ? null : str(body.get("jobName"));
        LocalDate workDate = parseDate(body == null ? null : str(body.get("workDate")));
        String note = body == null ? null : str(body.get("note"));
        return Result.success(talentPoolService.hireTalent(bossId, id, jobId, jobName, workDate, note));
    }

    @Operation(summary = "删除人才库条目", description = "同时删除关联评价与雇佣记录")
    @DeleteMapping("/{id}")
    public Result<Void> removeTalent(@PathVariable Long id, Authentication authentication) {
        Long bossId = requireBossId(authentication);
        talentPoolService.removeTalent(bossId, id);
        return Result.success();
    }

    @Operation(summary = "人才雇主评价列表", description = "按人才 id 返回评价列表")
    @GetMapping("/{id}/reviews")
    public Result<List<TalentReview>> listReviews(@PathVariable Long id, Authentication authentication) {
        requireBossId(authentication);
        return Result.success(talentPoolService.listReviews(id));
    }

    private Long requireBossId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser
                && UserRole.BOSS.equals(loginUser.role()) && loginUser.id() != null) {
            return loginUser.id();
        }
        throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号");
    }

    private Long toLong(Object value, String field) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 必须是整数");
        }
    }

    private String str(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDate.parse(value);
    }
}

package com.kuaima.app.controller.talent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.talent.entity.TalentFavorite;
import com.kuaima.app.domain.talent.repository.TalentFavoriteRepository;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;

/**
 * 老板端人才管理：搜索零工、收藏、历史合作、邀请、黑名单。
 */
@RestController
@RequestMapping("/talent")
public class TalentController {

    private final UserRepository userRepository;
    private final TalentFavoriteRepository favoriteRepository;
    private final BaseOrderItemRespository itemRepository;
    private final MessageService messageService;

    public TalentController(UserRepository userRepository,
                            TalentFavoriteRepository favoriteRepository,
                            BaseOrderItemRespository itemRepository,
                            MessageService messageService) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.itemRepository = itemRepository;
        this.messageService = messageService;
    }

    /**
     * 搜索零工：GET /talent/search?keyword=&skill=&city=&page=0&size=20
     * keyword 模糊匹配昵称/手机号/用户名；skill 匹配技能标签；city 匹配城市
     */
    @GetMapping("/search")
    public Result<List<User>> searchTalent(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String skill,
                                            @RequestParam(required = false) String city,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> result = userRepository.searchByRole("USER", null, keyword, pageable);
        List<User> filtered = result.getContent().stream()
                .filter(u -> !StringUtils.hasText(skill) || (u.getSkills() != null && u.getSkills().contains(skill)))
                .filter(u -> !StringUtils.hasText(city) || (u.getCity() != null && u.getCity().contains(city)))
                .collect(Collectors.toList());
        // total 取当前页过滤后的实际数量（原型阶段按页过滤，全量精确计数需扩展 Repository）
        return Result.success(filtered, result.getNumber(), filtered.size());
    }

    /**
     * 收藏人才列表：GET /talent/favorites?bossId=1
     * 返回收藏记录关联的零工详情。
     */
    @GetMapping("/favorites")
    public Result<List<Map<String, Object>>> listFavorites(@RequestParam Long bossId) {
        List<TalentFavorite> favorites = favoriteRepository.findByBossIdOrderByIdDesc(bossId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (TalentFavorite fav : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("favoriteId", fav.getId());
            userRepository.findById(fav.getWorkerId()).ifPresent(worker -> item.put("worker", worker));
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 收藏零工：POST /talent/favorites
     * body: { "bossId": 1, "workerId": 2 }
     */
    @PostMapping("/favorites")
    @Transactional
    public Result<TalentFavorite> favoriteWorker(@RequestBody Map<String, Long> body) {
        Long bossId = body.get("bossId");
        Long workerId = body.get("workerId");
        if (bossId == null || workerId == null) {
            throw new IllegalArgumentException("bossId 和 workerId 不能为空");
        }
        if (!userRepository.existsById(workerId)) {
            throw new IllegalArgumentException("零工不存在: " + workerId);
        }
        if (favoriteRepository.existsByBossIdAndWorkerId(bossId, workerId)) {
            return Result.error(400, "已收藏过该零工");
        }
        TalentFavorite fav = new TalentFavorite();
        fav.setBossId(bossId);
        fav.setWorkerId(workerId);
        return Result.success(favoriteRepository.save(fav));
    }

    /**
     * 取消收藏：DELETE /talent/favorites/{id}
     */
    @DeleteMapping("/favorites/{id}")
    @Transactional
    public Result<Void> unfavoriteWorker(@PathVariable Long id) {
        favoriteRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 历史合作零工：GET /talent/history?bossId=1
     * 从该老板订单下的报名记录中提取去重零工。
     */
    @GetMapping("/history")
    public Result<List<User>> listHistory(@RequestParam Long bossId) {
        List<User> workers = itemRepository.findByBossIdJoinOrder(bossId).stream()
                .map(item -> userRepository.findById(item.getUserId()).orElse(null))
                .filter(u -> u != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(User::getId, u -> u, (a, b) -> a),
                        m -> new ArrayList<>(m.values())));
        return Result.success(workers);
    }

    /**
     * 邀请零工：POST /talent/invite
     * body: { "bossId": 1, "workerId": 2, "orderId": 3 }
     * 发送站内消息通知零工。
     */
    @PostMapping("/invite")
    @Transactional
    public Result<Map<String, Object>> inviteWorker(@RequestBody Map<String, Long> body) {
        Long bossId = body.get("bossId");
        Long workerId = body.get("workerId");
        Long orderId = body.get("orderId");
        if (bossId == null || workerId == null) {
            throw new IllegalArgumentException("bossId 和 workerId 不能为空");
        }
        User boss = userRepository.findById(bossId).orElse(null);
        String bossName = boss != null && StringUtils.hasText(boss.getCompanyName())
                ? boss.getCompanyName()
                : (boss != null ? boss.getNickname() : "老板");
        messageService.sendToUser(workerId, "BOSS_INVITE", "招聘邀请",
                bossName + " 邀请您加入他们的岗位，快去看看吧！",
                BizType.ORDER, orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("invited", true);
        return Result.success(result);
    }

    /**
     * 老板黑名单：GET /talent/blacklist?bossId=1
     * （当前复用 admin 黑名单或返回空列表，后续可扩展老板私有黑名单）
     */
    @GetMapping("/blacklist")
    public Result<List<User>> listBlacklist(@RequestParam Long bossId) {
        return Result.success(new ArrayList<>());
    }
}

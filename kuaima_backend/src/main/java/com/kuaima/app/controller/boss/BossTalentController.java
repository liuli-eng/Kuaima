package com.kuaima.app.controller.boss;

import java.util.*;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.kuaima.app.common.*;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.message.constant.BizType;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.talent.entity.TalentFavorite;
import com.kuaima.app.domain.talent.repository.TalentFavoriteRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;

@RestController
@RequestMapping("/boss/talents")
@Tag(name = "老板端人才库")
public class BossTalentController {
    private final UserRepository users;
    private final TalentFavoriteRepository favorites;
    private final BossOrderRespository orders;
    private final MessageService messages;

    public BossTalentController(UserRepository users, TalentFavoriteRepository favorites,
                                BossOrderRespository orders, MessageService messages) {
        this.users = users; this.favorites = favorites; this.orders = orders; this.messages = messages;
    }

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category, @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "false") boolean favoriteOnly, Authentication authentication) {
        Long bossId = bossId(authentication);
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page 必须大于等于0，size 范围为1-100");
        Integer[] years = levelYears(level);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> result = users.searchTalents(normalize(keyword), normalize(category), years[0], years[1], favoriteOnly, bossId, pageable);
        List<Map<String, Object>> records = result.getContent().stream().map(u -> view(u, bossId)).collect(Collectors.toList());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", result.getTotalElements());
        data.put("page", result.getNumber());
        return Result.success(data, result.getNumber(), result.getTotalElements());
    }

    @PutMapping("/{workerId}/favorite")
    @Transactional
    public Result<Map<String, Object>> favorite(@PathVariable Long workerId, @RequestBody(required = false) Map<String, Object> body, Authentication authentication) {
        Long bossId = bossId(authentication);
        User worker = users.findById(workerId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("人才不存在: " + workerId));
        if (!UserRole.USER.equals(worker.getRole())) throw new jakarta.persistence.EntityNotFoundException("人才不存在: " + workerId);
        boolean nowFavorite;
        boolean exists = favorites.existsByBossIdAndWorkerId(bossId, workerId);
        Object requested = body == null ? null : body.get("favorite");
        boolean target = requested == null ? !exists : Boolean.parseBoolean(requested.toString());
        if (exists && !target) {
            favorites.deleteByBossIdAndWorkerId(bossId, workerId); nowFavorite = false;
        } else if (!exists && target) {
            TalentFavorite f = new TalentFavorite(); f.setBossId(bossId); f.setWorkerId(workerId); favorites.save(f); nowFavorite = true;
        } else nowFavorite = exists;
        return Result.success(Map.of("workerId", workerId, "isFavorite", nowFavorite));
    }

    @PostMapping("/{workerId}/invite")
    @Transactional
    public Result<Map<String, Object>> invite(@PathVariable Long workerId, @RequestBody(required = false) Map<String, Object> body,
                                               Authentication authentication) {
        Long bossId = bossId(authentication);
        User worker = users.findById(workerId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("人才不存在: " + workerId));
        if (!UserRole.USER.equals(worker.getRole())) throw new jakarta.persistence.EntityNotFoundException("人才不存在: " + workerId);
        Long orderId = body == null ? null : toLong(body.get("orderId"), "orderId");
        if (orderId != null) {
            BossOrder order = orders.findById(orderId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("订单不存在: " + orderId));
            if (!Objects.equals(bossId, order.getCreateBy())) throw new ForbiddenBusinessException("无权操作该订单");
        }
        User boss = users.findById(bossId).orElseThrow(() -> new ForbiddenBusinessException("老板账号不存在"));
        String name = StringUtils.hasText(boss.getCompanyName()) ? boss.getCompanyName() : Optional.ofNullable(boss.getNickname()).orElse("老板");
        Map<String,Object> extra = new HashMap<>(); extra.put("bossName", name); extra.put("orderId", orderId == null ? "" : orderId);
        messages.sendToUser(workerId, UserRole.USER, "BOSS_INVITE", "招聘邀请", name + " 邀请您加入他们的岗位，快去看看吧！", BizType.ORDER, orderId, extra);
        return Result.success(Map.of("invited", true, "workerId", workerId));
    }

    private Map<String,Object> view(User u, Long bossId) {
        Map<String,Object> m = new LinkedHashMap<>(); m.put("id", u.getId()); m.put("nickname", u.getNickname()); m.put("avatar", u.getAvatar());
        m.put("experience", u.getWorkYears() == null ? null : u.getWorkYears() + "年"); m.put("category", u.getSkills());
        m.put("rating", u.getCreditScore() == null ? null : Math.round(u.getCreditScore() * 5.0 / 100.0 * 10.0) / 10.0);
        m.put("tags", u.getSkills() == null ? List.of() : Arrays.stream(u.getSkills().split(",")).map(String::trim).filter(StringUtils::hasText).toList());
        m.put("isFavorite", favorites.existsByBossIdAndWorkerId(bossId, u.getId())); return m;
    }
    private Long bossId(Authentication a) { if (a != null && a.getPrincipal() instanceof LoginUser l && UserRole.BOSS.equals(l.role()) && l.id() != null) return l.id(); throw new ForbiddenBusinessException("当前登录账号不是有效的老板账号"); }
    private String normalize(String s) { return StringUtils.hasText(s) ? s.trim() : null; }
    private Integer[] levelYears(String level) { if (!StringUtils.hasText(level)) return new Integer[]{null,null}; String v=level.trim().toLowerCase(); try { int n=Integer.parseInt(v); return new Integer[]{n,n}; } catch(Exception ignored) {} return switch(v) { case "junior","初级" -> new Integer[]{0,2}; case "mid","中级" -> new Integer[]{3,5}; case "senior","高级" -> new Integer[]{6,null}; default -> throw new IllegalArgumentException("level 参数无效"); }; }
    private Long toLong(Object v,String f) { if(v==null)return null; try{return v instanceof Number n?n.longValue():Long.valueOf(v.toString());}catch(Exception e){throw new IllegalArgumentException(f+" 必须是整数");} }
}

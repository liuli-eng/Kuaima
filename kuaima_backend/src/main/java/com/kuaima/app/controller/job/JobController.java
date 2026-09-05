package com.kuaima.app.controller.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.browsehistory.entity.BrowseHistory;
import com.kuaima.app.domain.browsehistory.repository.BrowseHistoryRepository;
import com.kuaima.app.domain.jobfavorite.entity.JobFavorite;
import com.kuaima.app.domain.jobfavorite.repository.JobFavoriteRepository;

/**
 * 零工端岗位收藏与浏览记录。
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobFavoriteRepository favoriteRepository;
    private final BrowseHistoryRepository browseHistoryRepository;
    private final BossOrderRespository orderRepository;

    public JobController(JobFavoriteRepository favoriteRepository,
                         BrowseHistoryRepository browseHistoryRepository,
                         BossOrderRespository orderRepository) {
        this.favoriteRepository = favoriteRepository;
        this.browseHistoryRepository = browseHistoryRepository;
        this.orderRepository = orderRepository;
    }

    // ==================== 收藏 ====================

    /** 收藏岗位列表：GET /jobs/favorites?userId=1 */
    @GetMapping("/favorites")
    public Result<List<Map<String, Object>>> listFavorites(@RequestParam Long userId) {
        List<JobFavorite> favorites = favoriteRepository.findByUserIdOrderByIdDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (JobFavorite fav : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("favoriteId", fav.getId());
            item.put("orderId", fav.getOrderId());
            orderRepository.findById(fav.getOrderId()).ifPresent(order -> item.put("order", order));
            result.add(item);
        }
        return Result.success(result);
    }

    /** 收藏岗位：POST /jobs/favorites  body: { "userId": 1, "orderId": 2 } */
    @PostMapping("/favorites")
    @Transactional
    public Result<JobFavorite> favoriteJob(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long orderId = body.get("orderId");
        if (userId == null || orderId == null) {
            throw new IllegalArgumentException("userId 和 orderId 不能为空");
        }
        if (favoriteRepository.existsByUserIdAndOrderId(userId, orderId)) {
            return Result.error(400, "已收藏过该岗位");
        }
        JobFavorite fav = new JobFavorite();
        fav.setUserId(userId);
        fav.setOrderId(orderId);
        return Result.success(favoriteRepository.save(fav));
    }

    /** 取消收藏：DELETE /jobs/favorites/{id} */
    @DeleteMapping("/favorites/{id}")
    @Transactional
    public Result<Void> unfavoriteJob(@PathVariable Long id) {
        favoriteRepository.deleteById(id);
        return Result.success();
    }

    /** 检查收藏状态：GET /jobs/favorites/check?userId=1&orderId=2 */
    @GetMapping("/favorites/check")
    public Result<Map<String, Object>> checkFavorite(@RequestParam Long userId, @RequestParam Long orderId) {
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favoriteRepository.existsByUserIdAndOrderId(userId, orderId));
        return Result.success(result);
    }

    // ==================== 浏览记录 ====================

    /** 浏览记录分页：GET /jobs/history?userId=1&page=0&size=20 */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> listBrowseHistory(@RequestParam Long userId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<BrowseHistory> result = browseHistoryRepository.findByUserIdOrderByViewedAtDesc(userId, pageable);
        List<Map<String, Object>> list = new ArrayList<>();
        for (BrowseHistory h : result.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("historyId", h.getId());
            item.put("orderId", h.getOrderId());
            item.put("viewedAt", h.getViewedAt());
            orderRepository.findById(h.getOrderId()).ifPresent(order -> item.put("order", order));
            list.add(item);
        }
        return Result.success(list, result.getNumber(), result.getTotalElements());
    }

    /** 记录浏览：POST /jobs/history  body: { "userId": 1, "orderId": 2 } */
    @PostMapping("/history")
    @Transactional
    public Result<BrowseHistory> recordBrowse(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long orderId = body.get("orderId");
        if (userId == null || orderId == null) {
            throw new IllegalArgumentException("userId 和 orderId 不能为空");
        }
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setOrderId(orderId);
        history.setViewedAt(new Timestamp(System.currentTimeMillis()));
        return Result.success(browseHistoryRepository.save(history));
    }

    /** 清空浏览记录：DELETE /jobs/history?userId=1 */
    @DeleteMapping("/history")
    @Transactional
    public Result<Void> clearBrowseHistory(@RequestParam Long userId) {
        browseHistoryRepository.deleteByUserId(userId);
        return Result.success();
    }
}

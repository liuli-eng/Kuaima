package com.kuaima.app.controller.finance;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.badge.entity.Badge;
import com.kuaima.app.domain.badge.entity.UserBadge;
import com.kuaima.app.domain.badge.repository.BadgeRepository;
import com.kuaima.app.domain.badge.repository.UserBadgeRepository;

/**
 * 勋章目录与用户已获得勋章。
 */
@RestController
@RequestMapping("/badges")
public class BadgeController {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeController(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    /** 勋章目录：GET /badges */
    @GetMapping
    public Result<List<Badge>> listBadges() {
        return Result.success(badgeRepository.findAll());
    }

    /** 用户已获得勋章：GET /badges/user/{userId} */
    @GetMapping("/user/{userId}")
    public Result<List<UserBadge>> listUserBadges(@PathVariable Long userId) {
        return Result.success(userBadgeRepository.findByUserId(userId));
    }
}

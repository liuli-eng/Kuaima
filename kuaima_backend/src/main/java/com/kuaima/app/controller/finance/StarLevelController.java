package com.kuaima.app.controller.finance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.starlevel.entity.UserStarLevel;
import com.kuaima.app.domain.starlevel.repository.UserStarLevelRepository;

/**
 * 用户星级等级。
 */
@RestController
@RequestMapping("/star-level")
@Tag(name = "星级", description = "用户星级评级")
public class StarLevelController {

    private final UserStarLevelRepository starLevelRepository;

    public StarLevelController(UserStarLevelRepository starLevelRepository) {
        this.starLevelRepository = starLevelRepository;
    }

    /** 当前星级+进度：GET /star-level/{userId} */
    @Operation(summary = "当前星级与进度", description = "返回用户星级与成长进度；首次查询不存在时自动创建 1 星 0 进度记录")
    @GetMapping("/{userId}")
    @Transactional
    public Result<UserStarLevel> getStarLevel(@PathVariable Long userId) {
        UserStarLevel level = starLevelRepository.findByUserId(userId).orElseGet(() -> {
            UserStarLevel l = new UserStarLevel();
            l.setUserId(userId);
            l.setLevel(1);
            l.setProgress(0);
            return starLevelRepository.save(l);
        });
        return Result.success(level);
    }
}

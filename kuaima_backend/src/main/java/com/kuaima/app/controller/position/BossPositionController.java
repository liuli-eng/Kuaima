package com.kuaima.app.controller.position;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
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
import com.kuaima.app.domain.position.entity.BossPosition;
import com.kuaima.app.domain.position.service.BossPositionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/boss/positions")
@Tag(name = "老板-岗位管理", description = "岗位的增删改查、统计、热度排行")
@RequiredArgsConstructor
public class BossPositionController {

    private final BossPositionService positionService;

    @Operation(summary = "岗位列表", description = "按关键字和状态筛选岗位列表，支持分页")
    @GetMapping
    public Result<List<BossPosition>> listPositions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BossPosition> result = positionService.listPositions(keyword, status, page, size);
        return Result.success(result.getContent(), result.getNumber(), result.getTotalElements());
    }

    @Operation(summary = "岗位详情", description = "根据ID查询岗位详情")
    @GetMapping("/{id}")
    public Result<BossPosition> getPosition(@PathVariable Long id) {
        return Result.success(positionService.getPosition(id));
    }

    @Operation(summary = "根据编码查询岗位", description = "根据岗位编码查询岗位详情")
    @GetMapping("/code/{code}")
    public Result<BossPosition> getPositionByCode(@PathVariable String code) {
        return Result.success(positionService.getPositionByCode(code));
    }

    @Operation(summary = "创建岗位", description = "新建一个岗位")
    @PostMapping
    public Result<BossPosition> createPosition(@RequestBody BossPosition position) {
        return Result.success(positionService.createPosition(position));
    }

    @Operation(summary = "更新岗位", description = "更新岗位信息")
    @PutMapping("/{id}")
    public Result<BossPosition> updatePosition(@PathVariable Long id, @RequestBody BossPosition position) {
        return Result.success(positionService.updatePosition(id, position));
    }

    @Operation(summary = "删除岗位", description = "删除指定岗位")
    @DeleteMapping("/{id}")
    public Result<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return Result.success();
    }

    @Operation(summary = "切换岗位状态", description = "切换岗位的在招/停用状态")
    @PostMapping("/{id}/toggle-status")
    public Result<BossPosition> toggleStatus(@PathVariable Long id) {
        return Result.success(positionService.toggleStatus(id));
    }

    @Operation(summary = "岗位统计", description = "获取岗位统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(positionService.getStatistics());
    }

    @Operation(summary = "热度排行", description = "获取岗位热度排行")
    @GetMapping("/hot-rankings")
    public Result<List<BossPosition>> getHotRankings(
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(positionService.getHotRankings(limit));
    }
}

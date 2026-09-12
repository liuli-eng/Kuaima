package com.kuaima.app.controller.learn;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Notice;
import com.kuaima.app.admin.repository.NoticeRepository;
import com.kuaima.app.common.Result;

/**
 * 端侧公开公告（仅返回已发布公告）。
 */
@RestController
@RequestMapping("/notices")
@Tag(name = "规则公示", description = "对外公开的规则与公告查询")
public class NoticePublicController {

    private final NoticeRepository noticeRepository;

    public NoticePublicController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    /** 公告列表：GET /notices?scope=&page=0&size=20 */
    @Operation(summary = "公告列表", description = "仅返回已发布公告，内存分页。参数：scope(可选，公告范围过滤)、page(默认0)、size(默认20,上限100)；返回当页数据与总数")
    @GetMapping
    public Result<List<Notice>> listNotices(@RequestParam(required = false) String scope,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        List<Notice> all = noticeRepository.findAll().stream()
                .filter(n -> "已发布".equals(n.getStatus()))
                .filter(n -> scope == null || scope.isEmpty() || scope.equals(n.getScope()))
                .toList();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int from = Math.min(safePage * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return Result.success(all.subList(from, to), safePage, all.size());
    }
}
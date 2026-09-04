package com.kuaima.app.controller.chat;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Notice;
import com.kuaima.app.admin.repository.NoticeRepository;
import com.kuaima.app.common.Result;

import lombok.RequiredArgsConstructor;

/**
 * 公告中心：查询后台已发布公告，按 scope 过滤，倒序分页。
 * Notice 实体位于 admin 包，本接口做只读消费。
 */
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private static final String PUBLISHED = "已发布";

    private final NoticeRepository noticeRepository;

    /** 已发布公告列表：scope 为空表示全部范围 */
    @GetMapping
    public Result<List<Notice>> list(@RequestParam(required = false) String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : size;
        // 全量拉取后内存过滤（公告数量有限），保持按 id 倒序
        List<Notice> all = noticeRepository.findAll();
        List<Notice> filtered = all.stream()
                .filter(n -> PUBLISHED.equals(n.getStatus()))
                .filter(n -> scope == null || scope.isBlank() || scope.equals(n.getScope()))
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .collect(Collectors.toList());
        int total = filtered.size();
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);
        return Result.success(filtered.subList(from, to), safePage, total);
    }
}

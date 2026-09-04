package com.kuaima.app.controller.learn;

import java.util.List;

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
public class NoticePublicController {

    private final NoticeRepository noticeRepository;

    public NoticePublicController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    /** 公告列表：GET /notices?scope=&page=0&size=20 */
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

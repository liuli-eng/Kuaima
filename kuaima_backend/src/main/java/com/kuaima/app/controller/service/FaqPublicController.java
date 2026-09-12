package com.kuaima.app.controller.service;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.faq.entity.Faq;
import com.kuaima.app.domain.faq.repository.FaqRepository;

import lombok.RequiredArgsConstructor;

/**
 * 客服 FAQ 公开接口（小程序端使用，仅返回已启用）
 */
@RestController
@RequestMapping("/service/faqs")
@RequiredArgsConstructor
@Tag(name = "常见问题", description = "FAQ 公开查询")
public class FaqPublicController {

    private final FaqRepository faqRepository;

    @Operation(summary = "已启用 FAQ 列表", description = "仅返回已启用的 FAQ，按 sortOrder 升序；category 可选，传入时按分类过滤（内存过滤）")
    @GetMapping
    public Result<List<Faq>> listEnabledFaqs(@RequestParam(required = false) String category) {
        List<Faq> list = faqRepository.findByEnabledTrueOrderBySortOrderAsc();
        if (category != null && !category.isEmpty()) {
            list = list.stream().filter(f -> category.equals(f.getCategory())).toList();
        }
        return Result.success(list);
    }
}

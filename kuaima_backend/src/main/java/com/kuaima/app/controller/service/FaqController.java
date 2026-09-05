package com.kuaima.app.controller.service;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.faq.entity.Faq;
import com.kuaima.app.domain.faq.repository.FaqRepository;

/**
 * 客服中心常见问题。
 */
@RestController
@RequestMapping("/faq")
public class FaqController {

    private final FaqRepository faqRepository;

    public FaqController(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    /** 常见问题列表：GET /faq?category= */
    @GetMapping
    public Result<List<Faq>> listFaq(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return Result.success(faqRepository.findByCategoryOrderBySortOrderAsc(category));
        }
        return Result.success(faqRepository.findAll());
    }
}

package com.kuaima.app.controller.learn;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;
import com.kuaima.app.common.Result;

import jakarta.persistence.EntityNotFoundException;

/**
 * 端侧公开规则中心（仅返回已发布规则）。
 */
@RestController
@RequestMapping("/rules")
public class RulePublicController {

    private final RulesRepository rulesRepository;

    public RulePublicController(RulesRepository rulesRepository) {
        this.rulesRepository = rulesRepository;
    }

    /** 规则列表：GET /rules?category=交易规则 */
    @GetMapping
    public Result<List<Rules>> listRules(@RequestParam(required = false) String category) {
        List<Rules> all = rulesRepository.findAll();
        List<Rules> published = all.stream()
                .filter(r -> "已发布".equals(r.getStatus()))
                .filter(r -> category == null || category.isEmpty() || category.equals(r.getCategory()))
                .toList();
        return Result.success(published);
    }

    /** 规则详情：GET /rules/{id} */
    @GetMapping("/{id}")
    public Result<Rules> getRule(@PathVariable Long id) {
        return Result.success(rulesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("规则不存在: " + id)));
    }
}

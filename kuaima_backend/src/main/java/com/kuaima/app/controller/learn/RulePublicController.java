package com.kuaima.app.controller.learn;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.kuaima.app.common.Result;
import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;


/**
 * 端侧公开规则中心（仅返回已发布规则）。
 */
@RestController
@RequestMapping("/rules")
@Tag(name = "规则公示", description = "对外公开的规则查询")
public class RulePublicController {

    private final RulePublicService rulePublicService;

    @Autowired
    public RulePublicController(RulePublicService rulePublicService) {
        this.rulePublicService = rulePublicService;
    }

    public RulePublicController(RulesRepository repository) {
        this(new RulePublicService(repository));
    }


    /** 规则列表：GET /rules?category=交易规则 */
    @Operation(summary = "规则列表", description = "仅返回已发布规则；category 可选，传入时按分类过滤")
    @GetMapping
    public Result<java.util.List<Rules>> listRules(@RequestParam(required = false) String category) {
        return Result.success(rulePublicService.list(category));
    }

    /** 规则详情：GET /rules/{id} */
    @Operation(summary = "规则详情", description = "按 id 查询规则详情")
    @GetMapping("/{id}")
    public Result<Rules> getRule(@PathVariable Long id) {
        return Result.success(rulePublicService.get(id));
    }
}

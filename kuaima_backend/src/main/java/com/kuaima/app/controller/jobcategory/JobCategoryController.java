package com.kuaima.app.controller.jobcategory;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.HotItem;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.IndustryItem;
import com.kuaima.app.domain.jobcategory.model.JobCategoryModels.SearchItem;
import com.kuaima.app.domain.jobcategory.service.JobCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/job-categories")
@Tag(name = "岗位分类", description = "行业、企业类型、工种及热门工种查询")
public class JobCategoryController {

    private final JobCategoryService jobCategoryService;

    public JobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    @Operation(summary = "岗位分类树", description = "返回启用的行业、企业类型和工种三级树，均按 sortNo、id 升序排列")
    @GetMapping("/tree")
    public Result<List<IndustryItem>> tree() {
        return Result.success(jobCategoryService.tree());
    }

    @Operation(summary = "热门工种", description = "返回启用的热门工种快捷入口")
    @GetMapping("/hot")
    public Result<List<HotItem>> hot() {
        return Result.success(jobCategoryService.hot());
    }

    @Operation(summary = "搜索工种", description = "按工种名称或描述关键词模糊搜索，size 默认 20、限制 1~100")
    @GetMapping("/search")
    public Result<List<SearchItem>> search(@RequestParam String keyword,
                                           @RequestParam(defaultValue = "20") int size) {
        return Result.success(jobCategoryService.search(keyword, size));
    }
}

package com.kuaima.app.controller.project;

import java.util.List;
import java.util.Map;

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
import com.kuaima.app.domain.project.entity.ProjectMember;
import com.kuaima.app.domain.project.service.ProjectMemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/projects/{projectId}/members")
@Tag(name = "老板-项目成员", description = "项目成员列表、统计、增删、状态变更")
public class ProjectMemberController {

    private final ProjectMemberService memberService;

    public ProjectMemberController(ProjectMemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    @Operation(summary = "项目成员列表（status: all/active/temp/left）")
    public Result<List<ProjectMember>> list(@PathVariable Long projectId,
            @RequestParam(required = false, defaultValue = "all") String status) {
        return Result.success(memberService.listMembers(projectId, status));
    }

    @GetMapping("/stats")
    @Operation(summary = "项目成员统计：全部/在职/离职/临时")
    public Result<Map<String, Long>> stats(@PathVariable Long projectId) {
        return Result.success(memberService.stats(projectId));
    }

    @PostMapping
    @Operation(summary = "添加项目成员")
    public Result<ProjectMember> add(@PathVariable Long projectId, @RequestBody ProjectMember member) {
        member.setProjectId(projectId);
        return Result.success(memberService.add(member, null));
    }

    @PutMapping("/{memberId}")
    @Operation(summary = "变更成员状态（active/temp/left）")
    public Result<ProjectMember> updateStatus(@PathVariable Long projectId, @PathVariable Long memberId,
            @RequestParam String status) {
        return Result.success(memberService.updateStatus(memberId, status));
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "移除成员")
    public Result<Void> remove(@PathVariable Long projectId, @PathVariable Long memberId) {
        memberService.remove(memberId);
        return Result.success();
    }
}

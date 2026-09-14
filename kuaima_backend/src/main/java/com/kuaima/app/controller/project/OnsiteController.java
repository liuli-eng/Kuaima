package com.kuaima.app.controller.project;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.project.entity.ProjectOnsiteStaff;
import com.kuaima.app.domain.project.service.OnsiteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/boss/projects/{projectId}/onsite")
@Tag(name = "老板-驻场管理", description = "驻场人员列表、添加、移除")
public class OnsiteController {

    private final OnsiteService onsiteService;

    public OnsiteController(OnsiteService onsiteService) {
        this.onsiteService = onsiteService;
    }

    @GetMapping
    @Operation(summary = "驻场人员列表")
    public Result<List<ProjectOnsiteStaff>> list(@PathVariable Long projectId) {
        return Result.success(onsiteService.list(projectId));
    }

    @PostMapping
    @Operation(summary = "添加驻场人员（leader/assistant）")
    public Result<ProjectOnsiteStaff> add(@PathVariable Long projectId, @RequestBody ProjectOnsiteStaff staff) {
        staff.setProjectId(projectId);
        return Result.success(onsiteService.add(staff, null));
    }

    @DeleteMapping("/{onsiteId}")
    @Operation(summary = "移除驻场人员")
    public Result<Void> remove(@PathVariable Long projectId, @PathVariable Long onsiteId) {
        onsiteService.remove(onsiteId);
        return Result.success();
    }
}

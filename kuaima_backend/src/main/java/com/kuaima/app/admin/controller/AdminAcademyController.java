package com.kuaima.app.admin.controller;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kuaima.app.admin.repository.AdminUserRepository;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.common.Result;
import com.kuaima.app.domain.academy.model.AcademyModels.LessonVideoRequest;
import com.kuaima.app.domain.academy.model.AcademyModels.QuizRequest;
import com.kuaima.app.domain.academy.model.AcademyModels.SimulateVideoRequest;
import com.kuaima.app.domain.academy.model.AcademyModels.UploadResponse;
import com.kuaima.app.domain.academy.service.AcademyAdminService;
import com.kuaima.app.security.model.LoginUser;

/** 管理后台学堂内容管理。 */
@RestController
@RequestMapping("/admin/academy")
@Tag(name = "后台-学堂管理", description = "模拟接单视频、答题题库和新手课程管理")
public class AdminAcademyController {

    private final AcademyAdminService service;
    private final AdminUserRepository adminUsers;

    public AdminAcademyController(AcademyAdminService service, AdminUserRepository adminUsers) {
        this.service = service;
        this.adminUsers = adminUsers;
    }

    @GetMapping("/simulate-videos")
    @Operation(summary = "模拟接单视频列表", description = "按 sort ASC、id ASC 返回")
    public Result<List<Map<String, Object>>> simulateVideos(Authentication authentication) {
        requireAdmin(authentication, false);
        return Result.success(service.simulateVideos());
    }

    @PostMapping("/simulate-videos")
    @Operation(summary = "新增模拟接单视频", description = "通常先调用 /admin/academy/upload，再保存返回的 URL 和视频元数据")
    public Result<Map<String, Object>> createSimulateVideo(@RequestBody SimulateVideoRequest body,
                                                            Authentication authentication) {
        requireAdmin(authentication, true);
        return Result.success(service.createSimulateVideo(body));
    }

    @PutMapping("/simulate-videos/{id}")
    @Operation(summary = "更新模拟接单视频")
    public Result<Map<String, Object>> updateSimulateVideo(@PathVariable Long id,
                                                            @RequestBody SimulateVideoRequest body,
                                                            Authentication authentication) {
        requireAdmin(authentication, true);
        return Result.success(service.updateSimulateVideo(id, body));
    }

    @DeleteMapping("/simulate-videos/{id}")
    @Operation(summary = "删除模拟接单视频", description = "记录不存在时也返回成功，保证删除幂等")
    public Result<Void> deleteSimulateVideo(@PathVariable Long id, Authentication authentication) {
        requireAdmin(authentication, true);
        service.deleteSimulateVideo(id);
        return Result.success();
    }

    @PutMapping("/simulate-videos/{id}/toggle")
    @Operation(summary = "上下线模拟接单视频", description = "不带 enabled 时切换状态；传 enabled=true/false 时为幂等上下线")
    public Result<Map<String, Object>> toggleSimulateVideo(@PathVariable Long id,
                                                              @RequestParam(required = false) Boolean enabled,
                                                             Authentication authentication) {
        requireAdmin(authentication, true);
        return Result.success(service.toggleSimulateVideo(id, enabled));
    }

    @GetMapping("/quizzes")
    @Operation(summary = "答题题库列表", description = "支持 type=single/multi/judge 筛选，按 sort ASC、id ASC 返回")
    public Result<List<Map<String, Object>>> quizzes(@RequestParam(required = false) String type,
                                                      Authentication authentication) {
        requireAdmin(authentication, false);
        return Result.success(service.quizzes(type));
    }

    @PostMapping("/quizzes")
    @Operation(summary = "新增答题题目")
    public Result<Map<String, Object>> createQuiz(@RequestBody QuizRequest body,
                                                   Authentication authentication) {
        requireAdmin(authentication, true);
        return Result.success(service.createQuiz(body));
    }

    @PutMapping("/quizzes/{id}")
    @Operation(summary = "更新答题题目")
    public Result<Map<String, Object>> updateQuiz(@PathVariable Long id,
                                                    @RequestBody QuizRequest body,
                                                    Authentication authentication) {
        requireAdmin(authentication, true);
        return Result.success(service.updateQuiz(id, body));
    }

    @DeleteMapping("/quizzes/{id}")
    @Operation(summary = "删除答题题目", description = "记录不存在时也返回成功，保证删除幂等")
    public Result<Void> deleteQuiz(@PathVariable Long id, Authentication authentication) {
        requireAdmin(authentication, true);
        service.deleteQuiz(id);
        return Result.success();
    }

    @GetMapping("/lessons")
    @Operation(summary = "新手课程列表", description = "返回固定课程槽位及其视频状态")
    public Result<List<Map<String, Object>>> lessons(Authentication authentication) {
        requireAdmin(authentication, false);
        return Result.success(service.lessons());
    }

    @PostMapping("/lessons/{key}/video")
    @Operation(summary = "保存新手课程视频", description = "通常先调用 /admin/academy/upload，再保存返回的视频元数据")
    public Result<Map<String, Object>> uploadLessonVideo(@PathVariable String key,
                                                            @RequestBody LessonVideoRequest body,
                                                            Authentication authentication) {
        requireAdmin(authentication, true);
        return Result.success(service.uploadLessonVideo(key, body));
    }

    @DeleteMapping("/lessons/{key}/video")
    @Operation(summary = "删除新手课程视频", description = "视频不存在时也返回成功，保证删除幂等")
    public Result<Void> deleteLessonVideo(@PathVariable String key, Authentication authentication) {
        requireAdmin(authentication, true);
        service.deleteLessonVideo(key);
        return Result.success();
    }

    @PutMapping("/lessons/{key}/toggle")
    @Operation(summary = "上下线新手课程", description = "不带 enabled 时切换状态；传 enabled=true/false 时为幂等上下线")
    public Result<Map<String, Object>> toggleLesson(@PathVariable String key,
                                                       @RequestParam(required = false) Boolean enabled,
                                                       Authentication authentication) {
        requireAdmin(authentication, true);
        return Result.success(service.toggleLesson(key, enabled));
    }

    @PostMapping("/upload")
    @Operation(summary = "上传学堂视频", description = "multipart/form-data：file、title、type(simulate/lesson)；支持 MP4/MOV/WebM，最大 200MB")
    public Result<UploadResponse> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam String title,
                                          @RequestParam String type,
                                          Authentication authentication) {
        requireAdmin(authentication, true);
        return Result.success(service.upload(file, title, type));
    }

    private LoginUser requireAdmin(Authentication authentication, boolean write) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)
                || user.id() == null || user.role() == null || !user.role().startsWith("ADMIN_")) {
            throw new ForbiddenBusinessException("仅管理员可访问");
        }
        if (write && "ADMIN_VIEWER".equals(user.role())) {
            throw new ForbiddenBusinessException("当前管理员无学堂管理权限");
        }
        var admin = adminUsers.findById(user.id())
                .orElseThrow(() -> new ForbiddenBusinessException("管理员账号不存在"));
        if ("禁用".equals(admin.getStatus()) || "DISABLED".equalsIgnoreCase(admin.getStatus())) {
            throw new ForbiddenBusinessException("管理员账号已禁用");
        }
        return user;
    }
}

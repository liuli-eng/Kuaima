package com.kuaima.app.controller.learn;

import com.kuaima.app.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/worker/classroom")
@Tag(name = "零工课堂", description = "零工课堂首页内容聚合")
public class WorkerClassroomController {
    private final WorkerClassroomService service;
    public WorkerClassroomController(WorkerClassroomService service) { this.service = service; }

    @GetMapping({"", "/overview"})
    @Operation(summary = "课堂首页聚合", description = "返回模拟接单视频、学习规则、答题测试、如何接单和平台规则")
    public Result<Map<String, Object>> overview() { return Result.success(service.overview()); }

    @GetMapping("/quiz")
    @Operation(summary = "获取零工课堂答题题目", description = "只返回题目和选项，不返回正确答案")
    public Result<Map<String, Object>> quiz(Authentication authentication) {
        requireWorker(authentication);
        return Result.success(service.quiz());
    }

    @PostMapping("/quiz/submit")
    @Operation(summary = "提交零工课堂答题")
    public Result<Map<String, Object>> submitQuiz(@RequestBody Map<String, ?> body,
                                                   Authentication authentication) {
        requireWorker(authentication);
        Object rawAnswers = body == null ? null : body.get("answers");
        if (rawAnswers != null && !(rawAnswers instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("answers 必须是对象");
        }
        @SuppressWarnings("unchecked")
        Map<String, ?> answers = rawAnswers instanceof Map<?, ?> map ? (Map<String, ?>) map : null;
        return Result.success(service.submitQuiz(answers));
    }

    private void requireWorker(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)
                || user.id() == null || !UserRole.USER.equals(user.role())) {
            throw new ForbiddenBusinessException("仅零工账号可访问课堂答题");
        }
    }
}

package com.kuaima.app.admin.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import com.kuaima.app.admin.entity.AdminLog;
import com.kuaima.app.admin.repository.AdminLogRepository;

/**
 * 管理员操作日志写入服务。
 * 所有写操作都用 try-catch 包裹，确保日志记录失败不影响主业务流程。
 */
@Service
public class AdminLogService {

    private final AdminLogRepository repo;

    public AdminLogService(AdminLogRepository repo) {
        this.repo = repo;
    }

    public void record(String operator, Long operatorId, String type, String target, String ip, String result, String detail) {
        try {
            AdminLog log = new AdminLog();
            log.setOperator(operator != null ? operator : "system");
            log.setOperatorId(operatorId);
            log.setType(type);
            log.setTarget(truncate(target, 100));
            log.setIp(truncate(ip, 50));
            log.setResult(result);
            log.setDetail(truncate(detail, 500));
            log.setCreateTime(LocalDateTime.now());
            repo.save(log);
        } catch (Exception e) {
            // 日志写入失败不影响主流程
            System.err.println("[AdminLog] 记录日志失败: " + e.getMessage());
        }
    }

    /** 后台通用操作日志不阻塞业务响应；登录等显式调用仍可使用同步 record。 */
    @Async
    public void recordAsync(String operator, Long operatorId, String type, String target, String ip, String result, String detail) {
        record(operator, operatorId, type, target, ip, result, detail);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}

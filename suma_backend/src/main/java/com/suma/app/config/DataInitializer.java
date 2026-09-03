package com.suma.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.suma.app.admin.entity.AdminUser;
import com.suma.app.admin.repository.AdminUserRepository;

import java.time.LocalDateTime;

/**
 * 启动时初始化 admin 默认账号（仅首次，幂等）
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!adminUserRepository.existsByUsername("admin")) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("超级管理员");
            admin.setRole("SUPER_ADMIN");
            admin.setDept("技术部");
            admin.setStatus("启用");
            admin.setPhone("13800000000");
            admin.setEmail("admin@kuaima.com");
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            adminUserRepository.save(admin);
            System.out.println("[DataInit] 默认 admin 账号已创建: admin / admin123");
        } else {
            System.out.println("[DataInit] admin 账号已存在，跳过初始化");
        }
    }
}

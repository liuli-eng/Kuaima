package com.suma.app.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.admin.entity.AdminSetting;

public interface AdminSettingRepository extends JpaRepository<AdminSetting, String> {
    List<AdminSetting> findByCategory(String category);
}

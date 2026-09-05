package com.kuaima.app.domain.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.chat.entity.NotificationSetting;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    /** 按用户查询单条通知设置 */
    Optional<NotificationSetting> findByUserId(Long userId);
}

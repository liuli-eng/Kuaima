package com.kuaima.app.controller.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.chat.entity.NotificationSetting;
import com.kuaima.app.domain.chat.repository.NotificationSettingRepository;

import lombok.RequiredArgsConstructor;

/**
 * 用户通知偏好设置：订单/活动/系统通知开关及提示音、震动。
 * 首次查询不存在时自动创建默认全开记录。
 */
@RestController
@RequestMapping("/notification-settings")
@RequiredArgsConstructor
@Tag(name = "通知设置", description = "用户消息通知偏好设置")
public class NotificationSettingController {

    private final NotificationSettingRepository notificationSettingRepository;

    /** 获取通知设置（不存在则创建默认） */
    @Operation(summary = "获取通知设置", description = "按用户 id 查询通知偏好；首次查询不存在时自动创建默认全开记录（订单/活动/系统通知、提示音、震动均开启）")
    @GetMapping("/{userId}")
    public Result<NotificationSetting> get(@PathVariable Long userId) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> notificationSettingRepository.save(defaults(userId)));
        return Result.success(setting);
    }

    /** 更新通知设置（整体覆盖） */
    @Operation(summary = "更新通知设置", description = "整体覆盖更新指定用户的通知偏好，字段：orderNotif、activityNotif、systemNotif、sound、vibrate；记录不存在时先初始化默认值再更新")
    @PutMapping("/{userId}")
    public Result<NotificationSetting> update(@PathVariable Long userId,
            @RequestBody NotificationSetting body) {
        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> defaults(userId));
        setting.setUserId(userId);
        setting.setOrderNotif(body.getOrderNotif());
        setting.setActivityNotif(body.getActivityNotif());
        setting.setSystemNotif(body.getSystemNotif());
        setting.setSound(body.getSound());
        setting.setVibrate(body.getVibrate());
        return Result.success(notificationSettingRepository.save(setting));
    }

    private NotificationSetting defaults(Long userId) {
        NotificationSetting setting = new NotificationSetting();
        setting.setUserId(userId);
        setting.setOrderNotif(true);
        setting.setActivityNotif(true);
        setting.setSystemNotif(true);
        setting.setSound(true);
        setting.setVibrate(true);
        return setting;
    }
}

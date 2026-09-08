package com.kuaima.app.admin.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.MessageTemplate;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {

    List<MessageTemplate> findByStatusOrderByUpdateTimeDesc(String status);

    List<MessageTemplate> findByEventOrderByUpdateTimeDesc(String event);

    /** 按状态过滤 + 分页（按更新时间倒序） */
    Page<MessageTemplate> findByStatus(String status, Pageable pageable);

    /** 按事件过滤 + 分页（按更新时间倒序） */
    Page<MessageTemplate> findByEvent(String event, Pageable pageable);

    /** 查找所有启用且定时发送的模板 */
    List<MessageTemplate> findByStatusAndSendWay(String status, String sendWay);
}

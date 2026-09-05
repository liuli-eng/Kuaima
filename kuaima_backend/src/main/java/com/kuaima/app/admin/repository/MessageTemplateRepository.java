package com.kuaima.app.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.MessageTemplate;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {

    List<MessageTemplate> findByStatusOrderByUpdateTimeDesc(String status);

    List<MessageTemplate> findByEventOrderByUpdateTimeDesc(String event);
}

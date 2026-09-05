package com.kuaima.app.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.QuickReply;

public interface QuickReplyRepository extends JpaRepository<QuickReply, Long> {

    List<QuickReply> findByEnabledTrueOrderBySortOrderAsc();

    List<QuickReply> findByCategoryOrderBySortOrderAsc(String category);
}

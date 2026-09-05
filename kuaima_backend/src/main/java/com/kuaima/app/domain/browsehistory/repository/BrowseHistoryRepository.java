package com.kuaima.app.domain.browsehistory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.browsehistory.entity.BrowseHistory;

public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {

    /** 某用户的浏览记录分页（按浏览时间倒序） */
    Page<BrowseHistory> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);

    /** 清空某用户的浏览记录 */
    void deleteByUserId(Long userId);
}

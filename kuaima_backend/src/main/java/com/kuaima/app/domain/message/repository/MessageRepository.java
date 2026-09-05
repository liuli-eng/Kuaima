package com.kuaima.app.domain.message.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.message.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** 某用户未读消息数 */
    long countByUserIdAndReadFlagFalse(Long userId);

    /** 某用户消息按时间倒序 */
    List<Message> findByUserIdOrderByIdDesc(Long userId);

    /** 某用户消息分页（id 倒序 = 最新在前） */
    Page<Message> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    /** 某用户按已读/未读过滤分页 */
    Page<Message> findByUserIdAndReadFlagOrderByIdDesc(Long userId, boolean readFlag, Pageable pageable);

    /** 某用户某类型消息分页（如系统通知 SYSTEM_NOTICE） */
    Page<Message> findByUserIdAndTypeOrderByIdDesc(Long userId, String type, Pageable pageable);

    /** 校验消息归属后取单条 */
    Optional<Message> findByIdAndUserId(Long id, Long userId);

    /** 某用户全部标记已读，返回更新条数 */
    @Modifying
    @Query("update Message m set m.readFlag = true, m.readTime = :now where m.userId = :userId and m.readFlag = false")
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}

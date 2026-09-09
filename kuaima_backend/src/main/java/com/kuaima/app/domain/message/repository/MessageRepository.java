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

    Page<Message> findByUserIdAndRoleOrderByIdDesc(Long userId, String role, Pageable pageable);

    Page<Message> findByUserIdAndRoleAndReadFlagOrderByIdDesc(Long userId, String role,
                                                               boolean readFlag, Pageable pageable);

    long countByUserIdAndRoleAndReadFlagFalse(Long userId, String role);

    /**
     * 身份消息查询，同时兼容历史上将老板业务消息误写为 USER 的数据。
     * BOSS 查询按 role=BOSS 或老板专属 type；USER 查询排除老板专属 type。
     */
    @Query("""
            select m from Message m
             where m.userId = :userId
               and ((:role = 'BOSS' and (m.role = 'BOSS' or m.type in :bossTypes))
                 or (:role = 'USER' and m.role = 'USER' and m.type not in :bossTypes))
             order by m.id desc
            """)
    Page<Message> findByUserIdAndEffectiveRole(@Param("userId") Long userId,
                                                @Param("role") String role,
                                                @Param("bossTypes") List<String> bossTypes,
                                                Pageable pageable);

    @Query("""
            select m from Message m
             where m.userId = :userId
               and m.readFlag = :readFlag
               and ((:role = 'BOSS' and (m.role = 'BOSS' or m.type in :bossTypes))
                 or (:role = 'USER' and m.role = 'USER' and m.type not in :bossTypes))
             order by m.id desc
            """)
    Page<Message> findByUserIdAndEffectiveRoleAndReadFlag(@Param("userId") Long userId,
                                                           @Param("role") String role,
                                                           @Param("readFlag") boolean readFlag,
                                                           @Param("bossTypes") List<String> bossTypes,
                                                           Pageable pageable);

    @Query("""
            select count(m) from Message m
             where m.userId = :userId
               and m.readFlag = false
               and ((:role = 'BOSS' and (m.role = 'BOSS' or m.type in :bossTypes))
                 or (:role = 'USER' and m.role = 'USER' and m.type not in :bossTypes))
            """)
    long countByUserIdAndEffectiveRoleAndReadFlagFalse(@Param("userId") Long userId,
                                                        @Param("role") String role,
                                                        @Param("bossTypes") List<String> bossTypes);

    /** 某用户某类型消息分页（如系统通知 SYSTEM_NOTICE） */
    Page<Message> findByUserIdAndTypeOrderByIdDesc(Long userId, String type, Pageable pageable);

    /** 校验消息归属后取单条 */
    Optional<Message> findByIdAndUserId(Long id, Long userId);

    /** 某用户全部标记已读，返回更新条数 */
    @Modifying
    @Query("update Message m set m.readFlag = true, m.readTime = :now where m.userId = :userId and m.readFlag = false")
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}

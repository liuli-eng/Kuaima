package com.suma.app.domain.boss.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.suma.app.domain.boss.entity.BossOrder;

public interface BossOrderRespository extends JpaRepository<BossOrder, Long> {

    /** 按 类型/状态/标题 组合过滤分页查询（参数为空表示不过滤） */
    @Query("""
            select o from BossOrder o
            where (:type is null or o.type = :type)
              and (:status is null or o.orderStatus = :status)
              and (:title is null or o.orderTitle like concat('%', :title, '%'))
            """)
    Page<BossOrder> search(@Param("type") String type,
                                @Param("status") String status,
                                @Param("title") String title,
                                Pageable pageable);
}

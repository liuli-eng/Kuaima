package com.suma.app.domain.boss.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.domain.boss.entity.BaseOrderItem;

public interface BaseOrderItemRespository extends JpaRepository<BaseOrderItem, Long> {

    /** 查询某订单的所有报名记录 */
    List<BaseOrderItem> findByOrderId(Long orderId);

    /** 查询某用户报名过的订单记录 */
    List<BaseOrderItem> findByUserId(Long userId);

    /** 查询某订单下某用户是否已报名 */
    boolean existsByOrderIdAndUserId(Long orderId, Long userId);
}

package com.kuaima.app.domain.reward.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.reward.entity.RewardRechargeOrder;

import jakarta.persistence.LockModeType;

public interface RewardRechargeOrderRepository extends JpaRepository<RewardRechargeOrder, Long> {
    Optional<RewardRechargeOrder> findByOrderNo(String orderNo);

    Optional<RewardRechargeOrder> findByOrderNoAndUserId(String orderNo, Long userId);

    Optional<RewardRechargeOrder> findByIdempotencyKey(String idempotencyKey);
    Page<RewardRechargeOrder> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);
    Page<RewardRechargeOrder> findByUserIdAndStatusOrderByCreatedAtDescIdDesc(Long userId, String status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from RewardRechargeOrder o where o.orderNo=:orderNo")
    Optional<RewardRechargeOrder> findByOrderNoForUpdate(@Param("orderNo") String orderNo);
}

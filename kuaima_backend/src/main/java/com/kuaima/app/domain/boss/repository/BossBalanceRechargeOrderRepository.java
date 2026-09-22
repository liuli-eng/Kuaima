package com.kuaima.app.domain.boss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.kuaima.app.domain.boss.entity.BossBalanceRechargeOrder;

public interface BossBalanceRechargeOrderRepository extends JpaRepository<BossBalanceRechargeOrder, Long> {

    Optional<BossBalanceRechargeOrder> findByOrderNo(String orderNo);
    Optional<BossBalanceRechargeOrder> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from BossBalanceRechargeOrder o where o.orderNo=:orderNo")
    Optional<BossBalanceRechargeOrder> findByOrderNoForUpdate(@Param("orderNo") String orderNo);

    List<BossBalanceRechargeOrder> findByBossIdOrderByIdDesc(Long bossId);

    List<BossBalanceRechargeOrder> findByBossIdAndStatusOrderByIdDesc(Long bossId, String status);
}

package com.kuaima.app.domain.boss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.boss.entity.BossBalanceRechargeOrder;

public interface BossBalanceRechargeOrderRepository extends JpaRepository<BossBalanceRechargeOrder, Long> {

    Optional<BossBalanceRechargeOrder> findByOrderNo(String orderNo);

    List<BossBalanceRechargeOrder> findByBossIdOrderByIdDesc(Long bossId);

    List<BossBalanceRechargeOrder> findByBossIdAndStatusOrderByIdDesc(Long bossId, String status);
}

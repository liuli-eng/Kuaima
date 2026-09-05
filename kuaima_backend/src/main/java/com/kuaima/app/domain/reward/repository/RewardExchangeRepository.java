package com.kuaima.app.domain.reward.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.reward.entity.RewardExchange;

public interface RewardExchangeRepository extends JpaRepository<RewardExchange, Long> {

    /** 按用户查兑换记录 */
    List<RewardExchange> findByUserId(Long userId);
}

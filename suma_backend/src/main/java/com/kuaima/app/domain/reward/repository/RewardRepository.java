package com.kuaima.app.domain.reward.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.reward.entity.Reward;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    /** 按状态查奖品 */
    List<Reward> findByStatus(String status);
}

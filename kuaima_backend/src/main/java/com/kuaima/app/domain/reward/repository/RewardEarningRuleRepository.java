package com.kuaima.app.domain.reward.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.reward.entity.RewardEarningRule;

public interface RewardEarningRuleRepository extends JpaRepository<RewardEarningRule, Long> {
    List<RewardEarningRule> findByEnabledTrueOrderBySortAscIdAsc();

    Optional<RewardEarningRule> findByCode(String code);
}

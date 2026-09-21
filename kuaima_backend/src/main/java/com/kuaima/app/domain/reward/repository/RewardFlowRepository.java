package com.kuaima.app.domain.reward.repository;

import com.kuaima.app.domain.reward.entity.RewardFlow;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RewardFlowRepository extends JpaRepository<RewardFlow, Long> {
    Optional<RewardFlow> findBySourceKey(String sourceKey);
    Page<RewardFlow> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);
    Page<RewardFlow> findByUserIdAndTypeOrderByCreatedAtDescIdDesc(Long userId, String type, Pageable pageable);
    Optional<RewardFlow> findByUserIdAndBizTypeAndBizId(Long userId, String bizType, Long bizId);
    @Query("select coalesce(sum(f.amount),0) from RewardFlow f where f.userId=:userId and f.type=:type")
    java.math.BigDecimal sumByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
}

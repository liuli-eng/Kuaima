package com.kuaima.app.domain.user.repository;

import java.util.List;
import java.sql.Timestamp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.user.entity.CreditFlow;

public interface CreditFlowRepository extends JpaRepository<CreditFlow, Long> {
    Page<CreditFlow> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    List<CreditFlow> findTop10ByUserIdOrderByTimestampDesc(Long userId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<CreditFlow> findByUserIdAndScoreTypeOrderByTimestampDesc(Long userId, String scoreType, Pageable pageable);

    List<CreditFlow> findByUserIdAndScoreTypeOrderByTimestampDesc(Long userId, String scoreType);

    @org.springframework.data.jpa.repository.Query("select coalesce(sum(f.delta),0) from CreditFlow f where f.userId=:userId and f.ruleCode=:ruleCode and f.timestamp>=:start and f.timestamp<:end")
    Long sumDeltaByRuleInPeriod(@org.springframework.data.repository.query.Param("userId") Long userId,
                                @org.springframework.data.repository.query.Param("ruleCode") String ruleCode,
                                @org.springframework.data.repository.query.Param("start") Timestamp start,
                                @org.springframework.data.repository.query.Param("end") Timestamp end);
}

package com.kuaima.app.domain.user.service;

import java.util.List;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kuaima.app.domain.user.entity.CreditFlow;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.CreditFlowRepository;
import com.kuaima.app.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/** 信用分/星级分统一计算内核：按业务幂等号写入流水并更新账户快照。 */
@Service
public class CreditScoreService {
    public static final String BOSS_CREDIT = "BOSS_CREDIT";
    public static final String WORKER_STAR = "WORKER_STAR";

    private final UserRepository users;
    private final CreditFlowRepository flows;

    public CreditScoreService(UserRepository users, CreditFlowRepository flows) {
        this.users = users;
        this.flows = flows;
    }

    @Transactional
    public boolean adjust(Long userId, String scoreType, int delta, String ruleCode,
                          String bizType, String idempotencyKey, String reason) {
        if (userId == null || delta == 0 || scoreType == null || idempotencyKey == null || idempotencyKey.isBlank()) return false;
        if (flows.existsByIdempotencyKey(idempotencyKey)) return false;
        User user = users.findByIdForUpdate(userId).orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));
        int before = scoreType.equals(WORKER_STAR) ? value(user.getStarScore()) : value(user.getCreditScore());
        int after = Math.max(0, before + delta);
        if (scoreType.equals(WORKER_STAR)) user.setStarScore(after); else user.setCreditScore(after);
        users.save(user);
        CreditFlow flow = new CreditFlow();
        flow.setUserId(userId); flow.setDelta(after - before); flow.setReason(reason); flow.setBizType(bizType);
        flow.setScoreType(scoreType); flow.setIdempotencyKey(idempotencyKey); flow.setBeforeScore(before);
        flow.setAfterScore(after); flow.setRuleCode(ruleCode);
        flows.save(flow);
        return true;
    }

    @Transactional(readOnly = true)
    public int score(Long userId, String scoreType) {
        User u = users.findById(userId).orElseThrow(() -> new EntityNotFoundException("用户不存在: " + userId));
        return scoreType.equals(WORKER_STAR) ? value(u.getStarScore()) : value(u.getCreditScore());
    }

    @Transactional(readOnly = true)
    public List<CreditFlow> flows(Long userId, String scoreType) {
        return flows.findByUserIdAndScoreTypeOrderByTimestampDesc(userId, scoreType);
    }

    @Transactional(readOnly = true)
    public int ruleDelta(Long userId, String ruleCode, LocalDateTime start, LocalDateTime end) {
        Long total = flows.sumDeltaByRuleInPeriod(userId, ruleCode, Timestamp.valueOf(start), Timestamp.valueOf(end));
        return total == null ? 0 : total.intValue();
    }

    private int value(Integer v) { return v == null ? 0 : v; }
}

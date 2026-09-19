package com.kuaima.app.admin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reward_fund_flow", indexes = {
        @Index(name = "idx_reward_fund_flow_campaign", columnList = "campaign_id"),
        @Index(name = "idx_reward_fund_flow_created", columnList = "created_at")
})
@Getter
@Setter
public class RewardFundFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "fund_account_id", nullable = false)
    private Long fundAccountId;
    @Column(nullable = false, length = 20)
    private String direction;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;
    @Column(name = "campaign_id", nullable = false, unique = true)
    private Long campaignId;
    @Column(length = 500)
    private String remark;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

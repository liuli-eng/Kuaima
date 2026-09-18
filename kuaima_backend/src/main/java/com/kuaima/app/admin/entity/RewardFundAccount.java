package com.kuaima.app.admin.entity;
import java.math.BigDecimal;import jakarta.persistence.*;import lombok.Getter;import lombok.Setter;
@Entity @Table(name="reward_fund_account") @Getter @Setter public class RewardFundAccount {@Id private Long id=1L;@Column(precision=18,scale=2)private BigDecimal balance=BigDecimal.ZERO;}

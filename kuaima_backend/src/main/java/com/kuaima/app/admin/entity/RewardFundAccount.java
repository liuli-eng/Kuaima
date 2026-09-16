package com.kuaima.app.admin.entity;
import jakarta.persistence.*;import lombok.Getter;import lombok.Setter;
@Entity @Table(name="reward_fund_account") @Getter @Setter public class RewardFundAccount {@Id private Long id=1L;private Long balance=0L;}

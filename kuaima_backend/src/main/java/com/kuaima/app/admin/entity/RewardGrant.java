package com.kuaima.app.admin.entity;
import java.time.LocalDateTime;import jakarta.persistence.*;import lombok.Getter;import lombok.Setter;
@Entity @Table(name="reward_grant",uniqueConstraints=@UniqueConstraint(name="uk_reward_grant_user",columnNames={"campaign_id","user_id"}),indexes=@Index(name="idx_reward_grant_status",columnList="status")) @Getter @Setter
public class RewardGrant {@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(name="campaign_id",nullable=false)private Long campaignId;@Column(name="user_id",nullable=false)private Long userId;private String userRole;private Long amount;private String status;private LocalDateTime grantedAt;@Column(length=500)private String errorMessage;}

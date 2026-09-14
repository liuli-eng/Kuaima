package com.kuaima.app.domain.boss.entity;
import com.kuaima.app.domain.base.entity.BaseEntity; import jakarta.persistence.*; import lombok.Getter; import lombok.Setter;
@Entity @Table(name="boss_recruit_account") @Getter @Setter public class BossRecruitAccount extends BaseEntity { @Column(nullable=false) private Long ownerUserId; @Column(nullable=false) private String name; private String avatar; @Column(length=20) private String authorizationType="PERSONAL"; @Column(length=20) private String workCode; @Column(length=20) private String leaveCode; private Boolean current=false; }

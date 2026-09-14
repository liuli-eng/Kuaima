package com.kuaima.app.domain.boss.entity;

import java.util.Date;
import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="boss_order_template") @Getter @Setter
public class BossOrderTemplate extends BaseEntity {
    @Column(nullable=false) private Long ownerUserId;
    @Column(nullable=false, length=100) private String templateName;
    private Long sourceOrderId;
    private String orderTitle;
    private String positionName;
    private Integer salaryAmount;
    private String salaryUnit;
    private Date workDate;
    private Date startTime;
    private Date endTime;
    private String address;
    private Integer recruitCount;
    private Integer duration;
    private String genderRequirement;
    private String experienceRequirement;
    @Column(length=500) private String tags;
}

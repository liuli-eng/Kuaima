package com.kuaima.app.domain.boss.entity;

import java.util.Date;
import java.math.BigDecimal;
import com.kuaima.app.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity @Table(name="boss_order_template") @Getter @Setter
public class BossOrderTemplate extends BaseEntity {
    public void setSalaryAmount(Integer value) { this.salaryAmount = value == null ? null : BigDecimal.valueOf(value); }
    public void setSalaryAmount(int value) { this.salaryAmount = BigDecimal.valueOf(value); }
    public void setSalaryAmount(BigDecimal value) { this.salaryAmount = value; }
    @Column(name="enterprise_id") private Long enterpriseId;
    @Column(nullable=false) private Long ownerUserId;
    @Column(nullable=false, length=100) private String templateName;
    private Long sourceOrderId;
    private String orderTitle;
    private String positionName;
    @Column(precision=18, scale=2) private BigDecimal salaryAmount;
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
    private String orderContent;
    private Long industryId;
    @Column(length=500) private String enterpriseTypeIds;
    @Column(length=500) private String jobIds;
    private Long jobCategoryId;
    private String signMode;
    private Boolean phoneNotify;
    private Boolean signNotify;
    private Boolean startRemind;
    private Boolean settleNotify;
    /** 创建模板时关联订单已邀请的零工 ID 快照（JSON 数组）。 */
    @Column(length=2000) private String invitedWorkerIds;
}

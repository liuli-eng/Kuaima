package com.kuaima.app.domain.boss.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** 老板商户账户（余额查询）。金额以「分」存储。 */
@Entity
@Table(name = "boss_merchant_account", indexes = {
        @Index(name = "idx_bma_boss", columnList = "boss_id")
})
@Setter
@Getter
public class BossMerchantAccount extends BaseEntity {

    @Column(nullable = false, comment = "所属老板 user.id")
    private Long bossId;

    @Column(comment = "账户名称，如 晴时科技")
    private String accountName;

    @Column(comment = "主体全称，如 上海晴时网络科技有限公司")
    private String subjectName;

    @Column(comment = "商户号")
    private String merchantNo;

    @Column(comment = "账户余额（分）")
    private Long balance = 0L;

    @Column(comment = "是否默认账户")
    private Boolean isDefault = true;

    @Column(comment = "状态:active正常/frozen冻结")
    private String status = "active";
}

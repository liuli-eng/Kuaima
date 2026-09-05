package com.kuaima.app.domain.user.entity;

import com.kuaima.app.domain.base.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "credit_flow")
@Getter
@Setter
public class CreditFlow extends BaseEntity {
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Integer delta; // positive=add, negative=subtract
    @Column(length = 200)
    private String reason;
    @Column(length = 50)
    private String bizType; // ORDER_COMPLETE, LATE, CANCEL, etc.
}

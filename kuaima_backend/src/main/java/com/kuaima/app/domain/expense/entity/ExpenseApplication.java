package com.kuaima.app.domain.expense.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "expense_application", indexes = {
        @Index(name = "idx_expense_application_boss_status_created", columnList = "boss_id,status,create_time"),
        @Index(name = "idx_expense_application_order", columnList = "order_id")
}, uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(name = "uk_expense_application_boss_idempotency",
                columnNames = {"boss_id", "idempotency_key"})
})
@Getter
@Setter
public class ExpenseApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "boss_id", nullable = false)
    private Long bossId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_title", length = 200)
    private String orderTitle;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String attachments;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "manual_review", nullable = false)
    private boolean manualReview;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "paid_time")
    private LocalDateTime paidTime;
}

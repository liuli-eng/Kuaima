package com.kuaima.app.domain.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.payroll.entity.PayrollOrder;

import jakarta.persistence.LockModeType;

public interface PayrollOrderRepository extends JpaRepository<PayrollOrder, Long>, JpaSpecificationExecutor<PayrollOrder> {

    List<PayrollOrder> findByStatus(String status);

    List<PayrollOrder> findByProjectId(Long projectId);

    List<PayrollOrder> findByCreatorIdOrderByIdDesc(Long creatorId);

    List<PayrollOrder> findByCreatorIdAndStatusOrderByIdDesc(Long creatorId, String status);

    /** 审批时锁定发薪单行：保证同一单只有一个事务能通过 pending 校验（防重复扣款）。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PayrollOrder o where o.id = :id")
    Optional<PayrollOrder> findByIdForUpdate(@Param("id") Long id);
}

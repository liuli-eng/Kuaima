package com.kuaima.app.domain.payroll.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.payroll.entity.PayrollDetail;

import jakarta.persistence.LockModeType;

public interface PayrollDetailRepository extends JpaRepository<PayrollDetail, Long>,
        JpaSpecificationExecutor<PayrollDetail> {

    List<PayrollDetail> findByPayrollId(Long payrollId);

    List<PayrollDetail> findByPayrollIdIn(List<Long> payrollIds);

    List<PayrollDetail> findByPhoneOrderByIdDesc(String phone);

    /** 审批时锁定明细行，防并发审批同时改明细状态。 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from PayrollDetail d where d.payrollId in :ids order by d.id")
    List<PayrollDetail> findByPayrollIdInForUpdate(@Param("ids") Collection<Long> ids);
}

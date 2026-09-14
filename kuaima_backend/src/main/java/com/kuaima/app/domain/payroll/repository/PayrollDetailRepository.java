package com.kuaima.app.domain.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.payroll.entity.PayrollDetail;

public interface PayrollDetailRepository extends JpaRepository<PayrollDetail, Long>,
        JpaSpecificationExecutor<PayrollDetail> {

    List<PayrollDetail> findByPayrollId(Long payrollId);
}

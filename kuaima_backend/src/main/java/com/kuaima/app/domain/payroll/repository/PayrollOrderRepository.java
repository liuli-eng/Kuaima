package com.kuaima.app.domain.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.payroll.entity.PayrollOrder;

public interface PayrollOrderRepository extends JpaRepository<PayrollOrder, Long>, JpaSpecificationExecutor<PayrollOrder> {

    List<PayrollOrder> findByStatus(String status);

    List<PayrollOrder> findByProjectId(Long projectId);
}

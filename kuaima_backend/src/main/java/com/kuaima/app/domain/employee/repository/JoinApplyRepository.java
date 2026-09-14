package com.kuaima.app.domain.employee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.kuaima.app.domain.employee.entity.JoinApply;

public interface JoinApplyRepository extends JpaRepository<JoinApply, Long>, JpaSpecificationExecutor<JoinApply> {

    List<JoinApply> findByStatus(String status);
}

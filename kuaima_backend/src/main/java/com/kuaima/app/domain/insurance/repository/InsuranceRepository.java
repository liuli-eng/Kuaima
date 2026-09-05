package com.kuaima.app.domain.insurance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.insurance.entity.Insurance;

public interface InsuranceRepository extends JpaRepository<Insurance, Long> {

    /** 按用户查保险记录 */
    List<Insurance> findByUserIdOrderByIdDesc(Long userId);
}

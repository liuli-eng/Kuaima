package com.kuaima.app.domain.contract.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.contract.entity.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    /** 按老板查合同列表 */
    List<Contract> findByBossIdOrderByIdDesc(Long bossId);
}

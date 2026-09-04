package com.kuaima.app.domain.deposit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.deposit.entity.Deposit;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

    /** 按用户查押金单 */
    List<Deposit> findByUserId(Long userId);
}

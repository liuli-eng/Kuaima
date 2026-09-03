package com.suma.app.domain.wallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.domain.wallet.entity.WalletFlow;

public interface WalletFlowRespository extends JpaRepository<WalletFlow, Long> {

    /** 按用户查流水（外部排序按 id 倒序） */
    List<WalletFlow> findByUserIdOrderByIdDesc(Long userId);
}

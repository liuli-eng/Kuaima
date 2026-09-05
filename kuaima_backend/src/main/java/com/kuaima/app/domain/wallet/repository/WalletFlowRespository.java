package com.kuaima.app.domain.wallet.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.wallet.entity.WalletFlow;

public interface WalletFlowRespository extends JpaRepository<WalletFlow, Long> {

    /** 按用户查流水（外部排序按 id 倒序） */
    List<WalletFlow> findByUserIdOrderByIdDesc(Long userId);

    /** 按用户分页查流水（按 id 倒序） */
    Page<WalletFlow> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    /** 按用户+方向查流水（按 id 倒序） */
    List<WalletFlow> findByUserIdAndDirectionOrderByIdDesc(Long userId, String direction);
}

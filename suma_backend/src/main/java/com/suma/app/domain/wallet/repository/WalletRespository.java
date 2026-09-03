package com.suma.app.domain.wallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.domain.wallet.entity.Wallet;

public interface WalletRespository extends JpaRepository<Wallet, Long> {

    /** 按用户查钱包 */
    Optional<Wallet> findByUserId(Long userId);
}

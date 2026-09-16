package com.kuaima.app.domain.wallet.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.wallet.entity.Wallet;

public interface WalletRespository extends JpaRepository<Wallet, Long> {

    /** 按用户查钱包 */
    Optional<Wallet> findByUserId(Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.userId=:userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") Long userId);
}

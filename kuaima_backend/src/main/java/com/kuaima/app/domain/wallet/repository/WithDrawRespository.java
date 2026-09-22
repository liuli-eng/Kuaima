package com.kuaima.app.domain.wallet.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.wallet.entity.WithDraw;

public interface WithDrawRespository extends JpaRepository<WithDraw, Long> {

    /** 按用户查提现单（最新在前） */
    List<WithDraw> findByUserIdOrderByIdDesc(Long userId);

    Optional<WithDraw> findByIdempotencyKey(String idempotencyKey);

    List<WithDraw> findTop20ByStatusAndApplyTimeBeforeAndMerchantBatchNoIsNotNullOrderByApplyTimeAsc(
            String status, LocalDateTime before);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select w from WithDraw w where w.id=:id")
    Optional<WithDraw> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    /** 按提现单查（该方法保持默认派生语义，可不写，仅供阅读） */
    // List<WithDraw> findByStatus(String status);
}

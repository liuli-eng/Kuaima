package com.kuaima.app.domain.wallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.wallet.entity.WithDraw;

public interface WithDrawRespository extends JpaRepository<WithDraw, Long> {

    /** 按用户查提现单（最新在前） */
    List<WithDraw> findByUserIdOrderByIdDesc(Long userId);

    /** 按提现单查（该方法保持默认派生语义，可不写，仅供阅读） */
    // List<WithDraw> findByStatus(String status);
}

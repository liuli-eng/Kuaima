package com.kuaima.app.domain.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.user.entity.CreditFlow;

public interface CreditFlowRepository extends JpaRepository<CreditFlow, Long> {
    Page<CreditFlow> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    List<CreditFlow> findTop10ByUserIdOrderByTimestampDesc(Long userId);
}

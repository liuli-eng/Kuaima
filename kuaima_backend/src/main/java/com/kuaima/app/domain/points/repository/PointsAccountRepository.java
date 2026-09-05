package com.kuaima.app.domain.points.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.points.entity.PointsAccount;

public interface PointsAccountRepository extends JpaRepository<PointsAccount, Long> {

    /** 按用户查积分账户 */
    Optional<PointsAccount> findByUserId(Long userId);
}

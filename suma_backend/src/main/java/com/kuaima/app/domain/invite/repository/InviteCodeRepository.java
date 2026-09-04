package com.kuaima.app.domain.invite.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.invite.entity.InviteCode;

public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

    /** 按用户查邀请码 */
    Optional<InviteCode> findByUserId(Long userId);

    /** 按邀请码查 */
    Optional<InviteCode> findByCode(String code);
}

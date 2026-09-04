package com.kuaima.app.domain.invite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.invite.entity.InviteRelation;

public interface InviteRelationRepository extends JpaRepository<InviteRelation, Long> {

    /** 按邀请人查邀请关系 */
    List<InviteRelation> findByInviterId(Long inviterId);
}

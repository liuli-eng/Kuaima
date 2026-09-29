package com.kuaima.app.domain.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.social.entity.SocialGroupMember;

public interface SocialGroupMemberRepository extends JpaRepository<SocialGroupMember, Long> {

    Optional<SocialGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    @Query("select m.userId from SocialGroupMember m where m.groupId = :groupId and m.confirmed = true order by m.joinedAt desc, m.id desc")
    List<Long> findUserIdsByGroupId(@Param("groupId") Long groupId, Pageable pageable);
}

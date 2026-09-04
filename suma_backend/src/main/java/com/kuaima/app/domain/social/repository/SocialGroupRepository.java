package com.kuaima.app.domain.social.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.social.entity.SocialGroup;

public interface SocialGroupRepository extends JpaRepository<SocialGroup, Long> {
}

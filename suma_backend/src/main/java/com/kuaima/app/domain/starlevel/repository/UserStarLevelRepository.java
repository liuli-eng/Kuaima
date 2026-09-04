package com.kuaima.app.domain.starlevel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.starlevel.entity.UserStarLevel;

public interface UserStarLevelRepository extends JpaRepository<UserStarLevel, Long> {

    /** 按用户查星级 */
    Optional<UserStarLevel> findByUserId(Long userId);
}

package com.kuaima.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Blacklist;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
}

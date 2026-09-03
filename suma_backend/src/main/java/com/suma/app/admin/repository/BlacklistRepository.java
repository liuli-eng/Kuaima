package com.suma.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.admin.entity.Blacklist;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {
}

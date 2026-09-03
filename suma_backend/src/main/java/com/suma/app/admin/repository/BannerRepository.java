package com.suma.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suma.app.admin.entity.Banner;

public interface BannerRepository extends JpaRepository<Banner, Long> {
}

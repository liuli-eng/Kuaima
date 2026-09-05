package com.kuaima.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Banner;

public interface BannerRepository extends JpaRepository<Banner, Long> {
}

package com.kuaima.app.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Rules;

public interface RulesRepository extends JpaRepository<Rules, Long> {
}

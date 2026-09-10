package com.kuaima.app.admin.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Rules;

public interface RulesRepository extends JpaRepository<Rules, Long> {

    List<Rules> findByStatusIn(Collection<String> statuses);

    List<Rules> findByStatusInAndCategory(Collection<String> statuses, String category);
}

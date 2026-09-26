package com.kuaima.app.admin.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kuaima.app.admin.entity.Rules;

public interface RulesRepository extends JpaRepository<Rules, Long> {

    @Query("""
            select r from Rules r
            where r.type = 'platform'
               or (r.type is null and (r.category is null or r.category <> '信用分规则'))
            order by r.id desc
            """)
    List<Rules> findPlatformRules();

    List<Rules> findByStatusIn(Collection<String> statuses);

    List<Rules> findByStatusInAndCategory(Collection<String> statuses, String category);
}

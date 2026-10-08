package com.kuaima.app.admin.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /** 后台规则分页查询；type/category 同时传入时按 OR 匹配，status 独立按 AND 过滤。 */
    @Query("""
            select r from Rules r
            where (:status is null
                   or r.status = :status
                   or (:status = 'published' and r.status = '已发布'))
              and ((:type is null and :category is null)
                   or (:type is not null and lower(coalesce(r.type, '')) = lower(:type))
                   or (:category is not null and r.category = :category))
            order by r.id desc
            """)
    Page<Rules> search(@Param("type") String type,
                       @Param("category") String category,
                       @Param("status") String status,
                       Pageable pageable);
}

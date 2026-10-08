package com.kuaima.app.domain.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;

import com.kuaima.app.domain.social.entity.SocialGroup;

public interface SocialGroupRepository extends JpaRepository<SocialGroup, Long> {

    @Query(value = """
            select g from SocialGroup g
            where coalesce(g.role, 'WORKER') = :role
              and coalesce(g.status, 'ACTIVE') = :status
              and coalesce(g.deleted, false) = false
            order by coalesce(g.sort, 0) asc, g.createdAt desc, g.id desc
            """, countQuery = """
            select count(g) from SocialGroup g
            where coalesce(g.role, 'WORKER') = :role
              and coalesce(g.status, 'ACTIVE') = :status
              and coalesce(g.deleted, false) = false
            """)
    Page<SocialGroup> findVisibleGroups(@Param("role") String role,
                                        @Param("status") String status,
                                        Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from SocialGroup g where g.id = :id")
    Optional<SocialGroup> findByIdForUpdate(@Param("id") Long id);
}

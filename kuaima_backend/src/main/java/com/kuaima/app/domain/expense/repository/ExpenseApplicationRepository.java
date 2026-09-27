package com.kuaima.app.domain.expense.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.expense.entity.ExpenseApplication;

public interface ExpenseApplicationRepository extends JpaRepository<ExpenseApplication, Long> {
    Optional<ExpenseApplication> findByBossIdAndIdempotencyKey(Long bossId, String idempotencyKey);

    @Query("""
            select e from ExpenseApplication e
            where e.bossId = :bossId
              and (:status is null or e.status = :status)
            order by e.createTime desc, e.id desc
            """)
    Page<ExpenseApplication> searchByBoss(@Param("bossId") Long bossId,
                                          @Param("status") String status,
                                          Pageable pageable);
}

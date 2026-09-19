package com.kuaima.app.domain.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kuaima.app.domain.payroll.entity.PayrollEmployee;

@Repository
public interface PayrollEmployeeRepository extends JpaRepository<PayrollEmployee, Long> {

    List<PayrollEmployee> findByBossIdOrderByIdDesc(Long bossId);

    @Query("SELECT e FROM PayrollEmployee e WHERE e.bossId = :bossId AND " +
           "(:keyword IS NULL OR e.name LIKE %:keyword% OR e.phone LIKE %:keyword%) " +
           "ORDER BY e.id DESC")
    List<PayrollEmployee> searchByBoss(@Param("bossId") Long bossId,
                                        @Param("keyword") String keyword);

    long countByBossId(Long bossId);

    long countByBossIdAndAddTimeAfter(Long bossId, java.util.Date since);

    long countByBossIdAndAddTimeBetween(Long bossId, java.util.Date start, java.util.Date end);
}

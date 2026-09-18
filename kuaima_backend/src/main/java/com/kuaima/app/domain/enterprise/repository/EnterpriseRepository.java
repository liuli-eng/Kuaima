package com.kuaima.app.domain.enterprise.repository;

import com.kuaima.app.domain.enterprise.entity.Enterprise;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    Optional<Enterprise> findByCompanyCode(String companyCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Enterprise e where e.id = :id")
    Optional<Enterprise> findByIdForUpdate(@Param("id") Long id);
}

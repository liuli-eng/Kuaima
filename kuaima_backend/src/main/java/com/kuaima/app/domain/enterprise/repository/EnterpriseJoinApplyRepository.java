package com.kuaima.app.domain.enterprise.repository;

import com.kuaima.app.domain.enterprise.entity.EnterpriseJoinApply;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnterpriseJoinApplyRepository extends JpaRepository<EnterpriseJoinApply, Long> {

    @Query("select a from EnterpriseJoinApply a where a.enterpriseId = :enterpriseId" +
            " and (:status is null or a.status = :status)" +
            " order by a.id desc")
    Page<EnterpriseJoinApply> search(@Param("enterpriseId") Long enterpriseId,
                                      @Param("status") String status,
                                      Pageable pageable);

    long countByEnterpriseIdAndStatus(Long enterpriseId, String status);

    Optional<EnterpriseJoinApply> findFirstByEnterpriseIdAndUserIdAndStatusOrderByIdDesc(
            Long enterpriseId, Long userId, String status);
}

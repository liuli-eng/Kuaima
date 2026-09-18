package com.kuaima.app.domain.enterprise.repository;

import com.kuaima.app.domain.enterprise.entity.EnterpriseMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnterpriseMemberRepository extends JpaRepository<EnterpriseMember, Long> {
    Optional<EnterpriseMember> findByEnterpriseIdAndUserId(Long enterpriseId, Long userId);
    List<EnterpriseMember> findByUserIdAndStatus(Long userId, String status);
    Optional<EnterpriseMember> findFirstByUserIdAndStatusOrderByIdAsc(Long userId, String status);
    boolean existsByEnterpriseIdAndUserIdAndStatus(Long enterpriseId, Long userId, String status);
}

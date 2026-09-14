package com.kuaima.app.domain.boss.repository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kuaima.app.domain.boss.entity.BossOrderTemplate;
public interface BossOrderTemplateRepository extends JpaRepository<BossOrderTemplate,Long> {
 Page<BossOrderTemplate> findByOwnerUserIdOrderByIdDesc(Long ownerUserId, Pageable pageable);
 Optional<BossOrderTemplate> findByOwnerUserIdAndTemplateName(Long ownerUserId,String templateName);
 Optional<BossOrderTemplate> findByIdAndOwnerUserId(Long id,Long ownerUserId);
 boolean existsByOwnerUserIdAndTemplateNameAndIdNot(Long ownerUserId,String templateName,Long id);
}

package com.kuaima.app.admin.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Certification;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /** 按用户查询认证审核记录（按 id 倒序，最新在前） */
    List<Certification> findByUserIdOrderByIdDesc(Long userId);

    java.util.Optional<Certification> findTopByUserIdAndTypeOrderByIdDesc(Long userId, String type);

    /** 按 type + status 过滤 + 分页 */
    Page<Certification> findByTypeAndStatus(String type, String status, Pageable pageable);

    /** 仅按 type 过滤 + 分页 */
    Page<Certification> findByType(String type, Pageable pageable);

    /** 仅按 status 过滤 + 分页 */
    Page<Certification> findByStatus(String status, Pageable pageable);
}

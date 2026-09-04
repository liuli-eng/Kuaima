package com.kuaima.app.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.admin.entity.Certification;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /** 按用户查询认证审核记录（按 id 倒序，最新在前） */
    List<Certification> findByUserIdOrderByIdDesc(Long userId);
}

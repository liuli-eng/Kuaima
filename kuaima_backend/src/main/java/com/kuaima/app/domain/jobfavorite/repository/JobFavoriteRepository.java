package com.kuaima.app.domain.jobfavorite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.jobfavorite.entity.JobFavorite;

public interface JobFavoriteRepository extends JpaRepository<JobFavorite, Long> {

    /** 某用户的收藏列表（最新在前） */
    List<JobFavorite> findByUserIdOrderByIdDesc(Long userId);

    /** 检查是否已收藏 */
    boolean existsByUserIdAndOrderId(Long userId, Long orderId);

    /** 取消收藏 */
    void deleteByUserIdAndOrderId(Long userId, Long orderId);
}

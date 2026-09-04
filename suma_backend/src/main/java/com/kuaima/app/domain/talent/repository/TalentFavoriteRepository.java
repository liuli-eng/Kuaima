package com.kuaima.app.domain.talent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.talent.entity.TalentFavorite;

public interface TalentFavoriteRepository extends JpaRepository<TalentFavorite, Long> {

    /** 某老板收藏的全部零工记录（最新在前） */
    List<TalentFavorite> findByBossIdOrderByIdDesc(Long bossId);

    /** 校验是否已收藏 */
    boolean existsByBossIdAndWorkerId(Long bossId, Long workerId);

    /** 取消收藏 */
    void deleteByBossIdAndWorkerId(Long bossId, Long workerId);
}

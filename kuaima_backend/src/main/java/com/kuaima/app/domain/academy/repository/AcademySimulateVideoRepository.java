package com.kuaima.app.domain.academy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.academy.entity.AcademySimulateVideo;

public interface AcademySimulateVideoRepository extends JpaRepository<AcademySimulateVideo, Long> {
    List<AcademySimulateVideo> findAllByOrderBySortAscIdAsc();

    /** 零工课堂仅返回已启用视频。 */
    List<AcademySimulateVideo> findByEnabledTrueOrderBySortAscIdAsc();

    Optional<AcademySimulateVideo> findByUrl(String url);

    /** 在一次 UPDATE 中同时校验管理员状态并切换视频，减少远程数据库串行往返。 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update academy_simulate_video v
            join admin_user a on a.id = :adminId
             and upper(coalesce(a.status, '')) not in ('禁用', 'DISABLED')
            set v.enabled = not v.enabled,
                v.updated_at = current_timestamp
            where v.id = :videoId
            """, nativeQuery = true)
    int toggleForActiveAdmin(@Param("videoId") Long videoId, @Param("adminId") Long adminId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update academy_simulate_video v
            join admin_user a on a.id = :adminId
             and upper(coalesce(a.status, '')) not in ('禁用', 'DISABLED')
            set v.enabled = :enabled, v.updated_at = current_timestamp
            where v.id = :videoId
            """, nativeQuery = true)
    int setEnabledForActiveAdmin(@Param("videoId") Long videoId,
                                 @Param("adminId") Long adminId,
                                 @Param("enabled") boolean enabled);
}

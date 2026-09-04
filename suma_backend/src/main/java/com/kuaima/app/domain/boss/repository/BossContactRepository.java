package com.kuaima.app.domain.boss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.boss.entity.BossContact;

public interface BossContactRepository extends JpaRepository<BossContact, Long> {

    /** 某老板的全部联系人（最新在前） */
    List<BossContact> findByUserIdOrderByIdDesc(Long userId);

    /** 某老板的默认联系人 */
    Optional<BossContact> findByUserIdAndIsDefaultTrue(Long userId);

    /** 清空某老板的默认标记（设置默认前调用） */
    @Modifying
    @Query("update BossContact c set c.isDefault = false where c.userId = :userId and c.isDefault = true")
    int clearDefaultByUserId(@Param("userId") Long userId);
}

package com.kuaima.app.domain.boss.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.boss.entity.BossAddress;

public interface BossAddressRepository extends JpaRepository<BossAddress, Long> {

    /** 某老板的全部地址（最新在前） */
    List<BossAddress> findByUserIdOrderByIdDesc(Long userId);

    /** 某老板的默认地址 */
    Optional<BossAddress> findByUserIdAndIsDefaultTrue(Long userId);

    /** 清空某老板的默认标记（设置默认前调用） */
    @Modifying
    @Query("update BossAddress a set a.isDefault = false where a.userId = :userId and a.isDefault = true")
    int clearDefaultByUserId(@Param("userId") Long userId);
}

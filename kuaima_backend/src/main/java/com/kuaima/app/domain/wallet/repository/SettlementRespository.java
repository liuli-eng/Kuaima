package com.kuaima.app.domain.wallet.repository;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kuaima.app.domain.wallet.entity.Settlement;

public interface SettlementRespository extends JpaRepository<Settlement, Long> {

    /** 某订单的结算单（最新在前） */
    List<Settlement> findByOrderIdOrderByIdDesc(Long orderId);

    List<Settlement> findByItemIdInOrderByIdDesc(Collection<Long> itemIds);

    /** 某零工的结算单（最新在前） */
    List<Settlement> findByWorkerIdOrderByIdDesc(Long workerId);

    /** 某报名记录是否存在待支付/已支付的结算单（防重复结算） */
    boolean existsByItemIdAndStatusIn(Long itemId, java.util.Collection<String> statuses);

    /** 老板名下最新结算记录（通过订单归属隔离）。 */
    @Query("select s from Settlement s join BossOrder o on s.orderId = o.id where o.createBy = :bossId order by s.id desc")
    Page<Settlement> findByBossId(@Param("bossId") Long bossId, Pageable pageable);
}

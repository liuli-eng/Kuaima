package com.kuaima.app.domain.subaccount.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.subaccount.entity.SubAccount;

public interface SubAccountRepository extends JpaRepository<SubAccount, Long> {

    /** 按主账号查子账号 */
    List<SubAccount> findByParentId(Long parentId);
}

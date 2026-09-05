package com.kuaima.app.domain.faq.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kuaima.app.domain.faq.entity.Faq;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    /** 按分类查询，按 sortOrder 正序 */
    List<Faq> findByCategoryOrderBySortOrderAsc(String category);

    /** 查询所有已启用的 FAQ，按 sortOrder 正序 */
    List<Faq> findByEnabledTrueOrderBySortOrderAsc();
}

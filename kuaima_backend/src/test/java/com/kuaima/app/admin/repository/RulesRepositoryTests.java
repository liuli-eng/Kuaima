package com.kuaima.app.admin.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.kuaima.app.admin.entity.Rules;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:rules;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RulesRepositoryTests {

    @Autowired
    private RulesRepository repository;

    @Test
    void searchMatchesCreditTypeOrCategoryAndPublishedStatuses() {
        repository.save(rule("类型命中", "credit", "其他", "published"));
        repository.save(rule("分类命中", "platform", "信用分规则", "已发布"));
        repository.save(rule("未发布", "credit", "其他", "草稿"));
        repository.save(rule("无分类命中", "platform", "其他", "published"));
        repository.flush();

        Page<Rules> result = repository.search("credit", "信用分规则", "published", PageRequest.of(0, 200));

        assertEquals(2, result.getTotalElements());
        assertEquals("分类命中", result.getContent().get(0).getTitle());
        assertEquals("类型命中", result.getContent().get(1).getTitle());
    }

    private Rules rule(String title, String type, String category, String status) {
        Rules rule = new Rules();
        rule.setTitle(title);
        rule.setType(type);
        rule.setCategory(category);
        rule.setStatus(status);
        return rule;
    }
}

package com.kuaima.app.domain.social.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.kuaima.app.domain.social.entity.SocialGroup;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:social_group;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SocialGroupRepositoryTests {

    @Autowired
    private SocialGroupRepository repository;

    @Test
    void visibleGroupsUseWorkerActiveAndSortOrder() {
        repository.save(group("第二群", "WORKER", "ACTIVE", 2));
        repository.save(group("第一群", "WORKER", "ACTIVE", 1));
        repository.save(group("老板群", "BOSS", "ACTIVE", 0));
        repository.save(group("禁用群", "WORKER", "DISABLED", 0));
        repository.flush();

        Page<SocialGroup> result = repository.findVisibleGroups("WORKER", "ACTIVE", PageRequest.of(0, 20));

        assertEquals(2, result.getTotalElements());
        assertEquals("第一群", result.getContent().get(0).getName());
        assertEquals("第二群", result.getContent().get(1).getName());
    }

    private SocialGroup group(String name, String role, String status, int sort) {
        SocialGroup group = new SocialGroup();
        group.setName(name);
        group.setRole(role);
        group.setStatus(status);
        group.setSort(sort);
        group.setCreatedAt(LocalDateTime.now());
        return group;
    }
}

package com.kuaima.app.domain.boss.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.model.BossOrderQuery;

@SpringBootTest
class BossOrderRepositoryFilterTests {

    @Autowired
    private BossOrderRespository repository;

    @Test
    void comprehensiveFilter_shouldExecuteAsDatabasePagedQuery() {
        BossOrderQuery filter = new BossOrderQuery();
        filter.setStatus("招工中");
        filter.setStartDate(LocalDate.of(2026, 9, 8));
        filter.setEndDate(LocalDate.of(2026, 9, 8));
        filter.setJobCategoryId(-1L);
        filter.setSalaryMin(100);
        filter.setSalaryMax(200);
        filter.setLongitude(new BigDecimal("116.397"));
        filter.setLatitude(new BigDecimal("39.908"));
        filter.setDistanceKm(5D);
        filter.setExperience("EXPERIENCED");
        filter.setGender("MALE");
        filter.setTags("包吃住,日结");
        filter.setTagMode("ALL");

        repository.findAll(
                BossOrderSpecifications.from(-1L, filter),
                PageRequest.of(0, 20));
    }

    @Test
    void anyTagFilter_shouldExecuteAsDatabasePagedQuery() {
        BossOrderQuery filter = new BossOrderQuery();
        filter.setTags("包吃住,日结");
        filter.setTagMode("ANY");

        repository.findAll(
                BossOrderSpecifications.from(-1L, filter),
                PageRequest.of(0, 20));
    }
}

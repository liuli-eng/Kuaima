package com.kuaima.app.admin.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;

class RulesControllerTests {

    @Test
    void platformPathShouldUsePlatformRuleQueryInsteadOfIdConversion() throws Exception {
        RulesRepository repository = mock(RulesRepository.class);
        Rules rule = new Rules();
        rule.setId(1L);
        rule.setTitle("平台服务协议");
        rule.setType("platform");
        when(repository.findPlatformRules()).thenReturn(List.of(rule));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new RulesController(repository)).build();

        mockMvc.perform(get("/admin/rules/platform"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].type").value("platform"));

        verify(repository).findPlatformRules();
    }

    @Test
    void listShouldFilterPublishedCreditRulesByTypeOrCategory() throws Exception {
        RulesRepository repository = mock(RulesRepository.class);
        Rules byType = new Rules();
        byType.setId(2L);
        byType.setType("credit");
        byType.setStatus("published");
        Rules byCategory = new Rules();
        byCategory.setId(1L);
        byCategory.setCategory("信用分规则");
        byCategory.setStatus("已发布");
        when(repository.search(org.mockito.ArgumentMatchers.eq("credit"),
                org.mockito.ArgumentMatchers.eq("信用分规则"),
                org.mockito.ArgumentMatchers.eq("published"),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(byType, byCategory), PageRequest.of(0, 200), 2));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new RulesController(repository)).build();

        mockMvc.perform(get("/admin/rules")
                        .param("type", "credit")
                        .param("category", "信用分规则")
                        .param("status", "published")
                        .param("page", "0")
                        .param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.data[0].type").value("credit"))
                .andExpect(jsonPath("$.data[1].category").value("信用分规则"));

        verify(repository).search(org.mockito.ArgumentMatchers.eq("credit"),
                org.mockito.ArgumentMatchers.eq("信用分规则"),
                org.mockito.ArgumentMatchers.eq("published"),
                org.mockito.ArgumentMatchers.any(Pageable.class));
    }
}

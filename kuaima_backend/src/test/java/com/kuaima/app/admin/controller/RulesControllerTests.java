package com.kuaima.app.admin.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
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
}

package com.kuaima.app.controller.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kuaima.app.domain.social.dto.SocialGroupJoinRequest;
import com.kuaima.app.domain.social.dto.SocialGroupView;
import com.kuaima.app.domain.social.service.SocialGroupService;
import com.kuaima.app.security.model.LoginUser;

class SocialGroupControllerTests {

    @Test
    void listPassesWorkerFiltersAndPaginationToService() throws Exception {
        SocialGroupService service = mock(SocialGroupService.class);
        SocialGroupView view = new SocialGroupView(1L, "上海日结交流群", "找活交流", "每日岗位", null,
                146, 200, null, "ACTIVE", false, false, List.of(), 1);
        LoginUser login = new LoginUser(7L, "worker", "USER");
        when(service.list(eq(login), eq("WORKER"), eq("ACTIVE"), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(view), PageRequest.of(0, 20), 1));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SocialGroupController(service)).build();

        mockMvc.perform(get("/social-groups")
                        .principal(new UsernamePasswordAuthenticationToken(login, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].name").value("上海日结交流群"));

        verify(service).list(eq(login), eq("WORKER"), eq("ACTIVE"), eq(0), eq(20));
    }

    @Test
    void joinUsesJwtUserAndConfirmationBody() throws Exception {
        SocialGroupService service = mock(SocialGroupService.class);
        SocialGroupView view = new SocialGroupView(1L, "上海日结交流群", "找活交流", null, "https://qrcode",
                147, 200, null, "ACTIVE", true, false, List.of(), 1);
        LoginUser login = new LoginUser(7L, "worker", "USER");
        when(service.join(eq(1L), eq(new SocialGroupJoinRequest("WORKER_GROUP_PAGE", true)), eq(login)))
                .thenReturn(view);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SocialGroupController(service)).build();

        mockMvc.perform(post("/social-groups/1/join")
                        .principal(new UsernamePasswordAuthenticationToken(login, null))
                        .contentType("application/json")
                        .content("{\"source\":\"WORKER_GROUP_PAGE\",\"confirmed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.joined").value(true));

        verify(service).join(eq(1L), eq(new SocialGroupJoinRequest("WORKER_GROUP_PAGE", true)), eq(login));
    }
}

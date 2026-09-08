package com.kuaima.app.controller.jobcategory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.kuaima.app.domain.jobcategory.model.JobCategoryAdminModels.CreateEnterpriseTypeRequest;
import com.kuaima.app.domain.jobcategory.model.JobCategoryAdminModels.CreateJobRequest;
import com.kuaima.app.domain.jobcategory.service.JobCategoryService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

class BossJobCategoryControllerTests {

    private JobCategoryService service;
    private BossJobCategoryController controller;

    @BeforeEach
    void setUp() {
        service = mock(JobCategoryService.class);
        controller = new BossJobCategoryController(service);
    }

    @Test
    void createEnterpriseType_shouldRequireBossIdentity() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(2L, "worker", UserRole.USER));

        assertThrows(IllegalStateException.class, () -> controller.createEnterpriseType(
                new CreateEnterpriseTypeRequest(1L, "电子厂", null), authentication));
    }

    @Test
    void createEnterpriseType_shouldUseRequestIndustry() {
        Authentication authentication = bossAuthentication();

        controller.createEnterpriseType(
                new CreateEnterpriseTypeRequest(1L, "电子厂", 3), authentication);

        verify(service).createEnterpriseType(1L, "电子厂", 3);
    }

    @Test
    void createJob_shouldUseSelectedIndustryAndEnterprise() {
        Authentication authentication = bossAuthentication();

        controller.createJob(
                new CreateJobRequest(1L, 11L, "普工", "流水线", null), authentication);

        verify(service).createJob(1L, 11L, "普工", "流水线", null);
    }

    private Authentication bossAuthentication() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(1L, "boss", UserRole.BOSS));
        return authentication;
    }
}

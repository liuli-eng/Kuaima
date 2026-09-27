package com.kuaima.app.admin.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.kuaima.app.domain.user.repository.CreditFlowRepository;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.user.service.CreditScoreService;

class AdminCreditControllerBeanTests {

    @Test
    void springShouldInstantiateControllerWithThreeDependencies() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(UserRepository.class, () -> mock(UserRepository.class));
            context.registerBean(CreditScoreService.class, () -> mock(CreditScoreService.class));
            context.registerBean(CreditFlowRepository.class, () -> mock(CreditFlowRepository.class));
            context.registerBean(AdminCreditController.class);
            context.refresh();

            assertNotNull(context.getBean(AdminCreditController.class));
        }
    }
}

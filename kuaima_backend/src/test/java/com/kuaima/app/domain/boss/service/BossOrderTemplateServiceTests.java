package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.entity.BossOrderTemplate;
import com.kuaima.app.domain.boss.model.BossOrderTemplateModels.CreateRequest;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.repository.BossOrderTemplateRepository;

class BossOrderTemplateServiceTests {
    private BossOrderTemplateRepository templates;
    private BossOrderRespository orders;
    private BossOrderTemplateService service;

    @BeforeEach
    void setUp() {
        templates = mock(BossOrderTemplateRepository.class);
        orders = mock(BossOrderRespository.class);
        service = new BossOrderTemplateService(templates, orders);
    }

    @Test
    void detail_shouldReturnTemplateAndCompleteSourceOrderFields() {
        BossOrderTemplate template = template(10L, 7L, "旧名称");
        BossOrder order = new BossOrder();
        order.setId(100L); order.setCreateBy(7L); order.setOrderTitle("电子厂普工"); order.setType("daily");
        order.setPostion("普工"); order.setSalary(220); order.setDuration(10); order.setOrderNum(20);
        order.setTags("包吃住"); order.setExperience("EXPERIENCED"); order.setGender("不限");
        when(templates.findById(10L)).thenReturn(Optional.of(template));
        when(orders.findById(100L)).thenReturn(Optional.of(order));

        var result = service.detail(7L, 10L);

        assertEquals(10L, result.id());
        assertEquals("旧名称", result.templateName());
        assertEquals("电子厂普工", result.orderTitle());
        assertEquals("daily", result.type());
        assertEquals("普工", result.postion());
        assertEquals(220, result.salary());
        assertEquals(10, result.duration());
        assertEquals(20, result.orderNum());
        assertEquals("包吃住", result.tags());
    }

    @Test
    void rename_shouldRejectDuplicateName() {
        BossOrderTemplate template = template(10L, 7L, "旧名称");
        when(templates.findById(10L)).thenReturn(Optional.of(template));
        when(templates.existsByOwnerUserIdAndTemplateNameAndIdNot(7L, "新名称", 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.rename(7L, 10L, "新名称"));
    }

    @Test
    void rename_shouldRejectBlankName() {
        BossOrderTemplate template = template(10L, 7L, "旧名称");
        when(templates.findById(10L)).thenReturn(Optional.of(template));
        assertThrows(IllegalArgumentException.class, () -> service.rename(7L, 10L, "  "));
    }

    @Test
    void detail_shouldRejectTemplateOwnedByAnotherBoss() {
        when(templates.findById(10L)).thenReturn(Optional.of(template(10L, 8L, "他人模板")));
        assertThrows(ForbiddenBusinessException.class, () -> service.detail(7L, 10L));
    }

    @Test
    void detail_shouldReturnNotFoundForMissingTemplate() {
        when(templates.findById(10L)).thenReturn(Optional.empty());
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> service.detail(7L, 10L));
    }

    @Test
    void create_shouldKeepExistingContractAndMapOrderFields() {
        BossOrder order = new BossOrder(); order.setId(100L); order.setCreateBy(7L); order.setOrderStatus(BossStatus.ORDER_RECRUITING);
        order.setOrderTitle("分拣"); order.setType("daily"); order.setPostion("分拣员"); order.setSalary(180); order.setDuration(8); order.setOrderNum(5);
        when(orders.findById(100L)).thenReturn(Optional.of(order));
        when(templates.findByOwnerUserIdAndTemplateName(7L, "模板")).thenReturn(Optional.empty());
        when(templates.save(any(BossOrderTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BossOrderTemplate result = service.create(7L, 100L, new CreateRequest("模板", false));

        assertEquals("模板", result.getTemplateName());
        assertEquals("分拣", result.getOrderTitle());
        assertEquals("daily", result.getSalaryUnit());
        assertEquals(8, result.getDuration());
    }

    private BossOrderTemplate template(Long id, Long owner, String name) {
        BossOrderTemplate template = new BossOrderTemplate(); template.setId(id); template.setOwnerUserId(owner);
        template.setTemplateName(name); template.setSourceOrderId(100L); return template;
    }
}

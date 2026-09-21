package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.entity.BossOrderTemplate;
import com.kuaima.app.domain.boss.model.BossOrderTemplateModels.CreateRequest;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.repository.BossOrderTemplateRepository;
import com.kuaima.app.domain.message.repository.MessageRepository;

class BossOrderTemplateServiceTests {
    private BossOrderTemplateRepository templates;
    private BossOrderRespository orders;
    private BossOrderTemplateService service;
    private MessageRepository messages;

    @BeforeEach
    void setUp() {
        templates = mock(BossOrderTemplateRepository.class);
        orders = mock(BossOrderRespository.class);
        messages = mock(MessageRepository.class);
        service = new BossOrderTemplateService(templates, orders, messages);
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
        when(messages.findDistinctUserIdsByTypeAndBizTypeAndBizId("BOSS_INVITE", "order", 100L))
                .thenReturn(java.util.List.of(21L, 22L));

        var result = service.detail(7L, 10L);

        assertEquals(10L, result.id());
        assertEquals("旧名称", result.templateName());
        assertEquals("电子厂普工", result.orderTitle());
        assertEquals("daily", result.type());
        assertEquals("普工", result.postion());
        assertEquals(new java.math.BigDecimal("220"), result.salary());
        assertEquals(10, result.duration());
        assertEquals(20, result.orderNum());
        assertEquals("包吃住", result.tags());
        assertEquals(java.util.List.of(21L, 22L), result.invitedWorkerIds());
    }

    @Test
    void list_shouldExposeSalaryAndRecruitCountWithFrontendFieldNames() {
        BossOrderTemplate template = template(10L, 7L, "普工模板");
        template.setSalaryAmount(220);
        template.setRecruitCount(12);
        when(templates.findByOwnerUserIdOrderByIdDesc(org.mockito.ArgumentMatchers.eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(template)));

        var result = service.list(7L, 0, 20).getContent().get(0);

        assertEquals(220, result.salary());
        assertEquals(12, result.orderNum());
    }

    @Test
    void detail_shouldReturnCompleteTemplateSnapshotWhenSourceOrderIsMissing() {
        BossOrderTemplate template = template(10L, 7L, "仓库模板");
        template.setOrderTitle("仓库分拣"); template.setOrderContent("负责货物分拣"); template.setSalaryUnit("daily");
        template.setIndustryId(1L); template.setEnterpriseTypeIds("[11,12]"); template.setJobIds("21,22"); template.setJobCategoryId(21L);
        template.setPositionName("分拣员"); template.setAddress("上海市浦东新区"); template.setSalaryAmount(220); template.setDuration(8); template.setRecruitCount(10);
        template.setSignMode("manual"); template.setPhoneNotify(false); template.setSignNotify(true); template.setStartRemind(false); template.setSettleNotify(true);
        template.setInvitedWorkerIds("[31,32]");
        when(templates.findById(10L)).thenReturn(Optional.of(template));
        when(orders.findById(100L)).thenReturn(Optional.empty());

        var result = service.detail(7L, 10L);

        assertEquals("负责货物分拣", result.orderContent());
        assertEquals(java.util.List.of(11L, 12L), result.enterpriseTypeIds());
        assertEquals(java.util.List.of(21L, 22L), result.jobIds());
        assertEquals("上海市浦东新区", result.address());
        assertEquals("manual", result.signMode());
        assertEquals(false, result.phoneNotify());
        assertEquals(java.util.List.of(31L, 32L), result.invitedWorkerIds());
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
        when(messages.findDistinctUserIdsByTypeAndBizTypeAndBizId("BOSS_INVITE", "order", 100L))
                .thenReturn(java.util.List.of(41L));

        BossOrderTemplate result = service.create(7L, 100L, new CreateRequest("模板", false));

        assertEquals("模板", result.getTemplateName());
        assertEquals("分拣", result.getOrderTitle());
        assertEquals("daily", result.getSalaryUnit());
        assertEquals(8, result.getDuration());
        assertEquals("[41]", result.getInvitedWorkerIds());
    }

    private BossOrderTemplate template(Long id, Long owner, String name) {
        BossOrderTemplate template = new BossOrderTemplate(); template.setId(id); template.setOwnerUserId(owner);
        template.setTemplateName(name); template.setSourceOrderId(100L); return template;
    }
}

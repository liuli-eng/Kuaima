package com.kuaima.app.domain.boss.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.model.BossOrderQuery;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;

class BossOrderServiceListTests {

    private BossOrderRespository orderRepository;
    private BaseOrderItemRespository itemRepository;
    private BossOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(BossOrderRespository.class);
        itemRepository = mock(BaseOrderItemRespository.class);
        service = new BossOrderService(
                orderRepository,
                itemRepository,
                mock(UserRepository.class),
                mock(MessageService.class),
                mock(SettlementRespository.class));
    }

    @Test
    void bossList_shouldFilterByOwnerAndFillActiveApplicantCount() {
        BossOrder order = new BossOrder();
        order.setId(11L);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(itemRepository.countByOrderIdsAndStatuses(eq(List.of(11L)), any()))
                .thenReturn(List.<Object[]>of(new Object[] {11L, 3L}));

        var result = service.listOrders(2L, null, "招工中", null, 0, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals(3L, result.getContent().get(0).getCurrentApply());
        verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(itemRepository).countByOrderIdsAndStatuses(
                eq(List.of(11L)),
                eq(List.of(
                        BossStatus.ITEM_APPLIED,
                        BossStatus.ITEM_HIRED,
                        BossStatus.ITEM_ON_WORK,
                        BossStatus.ITEM_FINISHED)));
    }

    @Test
    void publicList_shouldKeepExistingGlobalQueryAndReturnZeroWhenNoApplicants() {
        BossOrder order = new BossOrder();
        order.setId(12L);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(itemRepository.countByOrderIdsAndStatuses(eq(List.of(12L)), any()))
                .thenReturn(List.of());

        var result = service.listOrders(null, null, null, null, 0, 20);

        assertEquals(0L, result.getContent().get(0).getCurrentApply());
        verify(orderRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void list_shouldLimitPageSizeToOneHundred() {
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        BossOrderQuery query = new BossOrderQuery();
        query.setPage(-1);
        query.setSize(500);

        service.listOrders(2L, query);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAll(any(Specification.class), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void list_shouldRejectInvalidRangeAndDistanceParameters() {
        BossOrderQuery salaryQuery = new BossOrderQuery();
        salaryQuery.setSalaryMin(200);
        salaryQuery.setSalaryMax(100);
        assertThrows(IllegalArgumentException.class, () -> service.listOrders(2L, salaryQuery));

        BossOrderQuery distanceQuery = new BossOrderQuery();
        distanceQuery.setDistanceKm(5D);
        assertThrows(IllegalArgumentException.class, () -> service.listOrders(2L, distanceQuery));

        BossOrderQuery tagModeQuery = new BossOrderQuery();
        tagModeQuery.setTags("包吃住,日结");
        tagModeQuery.setTagMode("INVALID");
        assertThrows(IllegalArgumentException.class, () -> service.listOrders(2L, tagModeQuery));
    }
}

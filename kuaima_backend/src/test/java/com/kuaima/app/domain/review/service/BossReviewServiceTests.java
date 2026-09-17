package com.kuaima.app.domain.review.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.review.entity.BossReview;
import com.kuaima.app.domain.review.model.BossReviewModels.SaveRequest;
import com.kuaima.app.domain.review.repository.BossReviewRepository;
import com.kuaima.app.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

class BossReviewServiceTests {
    private BossReviewRepository reviews;
    private BaseOrderItemRespository items;
    private BossOrderRespository orders;
    private UserRepository users;
    private BossReviewService service;

    @BeforeEach
    void setUp() {
        reviews = mock(BossReviewRepository.class); items = mock(BaseOrderItemRespository.class);
        orders = mock(BossOrderRespository.class); users = mock(UserRepository.class);
        service = new BossReviewService(reviews, items, orders, users);
        when(reviews.save(any())).thenAnswer(invocation -> {
            BossReview value = invocation.getArgument(0); if (value.getId() == null) value.setId(1001L); return value;
        });
    }

    @Test
    void completedItem_shouldCreateReviewAndDeriveBossFromOrder() {
        prepareCompleted(30L, 2001L, 3001L, 4001L);
        when(reviews.findByItemId(2001L)).thenReturn(Optional.empty());

        var result = service.save(30L, 2001L, new SaveRequest(5, 4, 5, "  沟通顺畅  "));

        assertEquals(4001L, result.bossId()); assertEquals("沟通顺畅", result.content());
        verify(reviews).save(argThat(r -> r.getWorkerId().equals(30L) && r.getBossId().equals(4001L)));
    }

    @Test
    void repeatedPut_shouldUpdateExistingReview() {
        prepareCompleted(30L, 2001L, 3001L, 4001L);
        BossReview existing = review(1001L, 2001L, 3001L, 30L, 4001L);
        when(reviews.findByItemId(2001L)).thenReturn(Optional.of(existing));

        var result = service.save(30L, 2001L, new SaveRequest(3, 4, 2, "修改后"));

        assertEquals(1001L, result.id()); assertEquals(3, result.attitudeScore());
        assertEquals("修改后", existing.getContent());
    }

    @Test
    void invalidScores_shouldBeRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.save(30L, 1L, new SaveRequest(null, 4, 5, null)));
        assertThrows(IllegalArgumentException.class, () -> service.save(30L, 1L, new SaveRequest(0, 4, 5, null)));
        assertThrows(IllegalArgumentException.class, () -> service.save(30L, 1L, new SaveRequest(5, 6, 5, null)));
    }

    @Test
    void contentOverTwoHundredCharacters_shouldBeRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.save(30L, 1L, new SaveRequest(5, 4, 5, "好".repeat(201))));
    }

    @Test
    void itemOwnedByAnotherWorker_shouldBeForbidden() {
        when(users.existsById(30L)).thenReturn(true);
        BaseOrderItem item = item(1L, 2L, 31L, BossStatus.ITEM_FINISHED);
        when(items.findByIdForUpdate(1L)).thenReturn(Optional.of(item));
        assertThrows(ForbiddenBusinessException.class,
                () -> service.save(30L, 1L, new SaveRequest(5, 4, 5, null)));
    }

    @Test
    void nonCompletedItem_shouldReturnClearBusinessError() {
        when(users.existsById(30L)).thenReturn(true);
        when(items.findByIdForUpdate(1L)).thenReturn(Optional.of(item(1L, 2L, 30L, BossStatus.ITEM_ON_WORK)));
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.save(30L, 1L, new SaveRequest(5, 4, 5, null)));
        assertTrue(error.getMessage().contains("仅已完成"));
        assertTrue(error.getMessage().contains(BossStatus.ITEM_ON_WORK));
    }

    @Test
    void missingItem_shouldReturnNotFound() {
        when(users.existsById(30L)).thenReturn(true);
        when(items.findByIdForUpdate(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.save(30L, 999L, new SaveRequest(5, 4, 5, null)));
    }

    @Test
    void getWithoutReview_shouldReturnNullAfterOwnershipCheck() {
        when(users.existsById(30L)).thenReturn(true);
        when(items.findById(1L)).thenReturn(Optional.of(item(1L, 2L, 30L, BossStatus.ITEM_FINISHED)));
        when(reviews.findByItemId(1L)).thenReturn(Optional.empty());
        assertNull(service.get(30L, 1L));
    }

    private void prepareCompleted(Long workerId, Long itemId, Long orderId, Long bossId) {
        when(users.existsById(workerId)).thenReturn(true); when(users.existsById(bossId)).thenReturn(true);
        when(items.findByIdForUpdate(itemId)).thenReturn(Optional.of(item(itemId, orderId, workerId, BossStatus.ITEM_FINISHED)));
        BossOrder order = new BossOrder(); order.setId(orderId); order.setCreateBy(bossId);
        when(orders.findById(orderId)).thenReturn(Optional.of(order));
    }
    private BaseOrderItem item(Long id, Long orderId, Long workerId, String status) {
        BaseOrderItem item = new BaseOrderItem(); item.setId(id); item.setOrderId(orderId);
        item.setUserId(workerId); item.setStatus(status); return item;
    }
    private BossReview review(Long id, Long itemId, Long orderId, Long workerId, Long bossId) {
        BossReview r = new BossReview(); r.setId(id); r.setItemId(itemId); r.setOrderId(orderId);
        r.setWorkerId(workerId); r.setBossId(bossId); return r;
    }
}

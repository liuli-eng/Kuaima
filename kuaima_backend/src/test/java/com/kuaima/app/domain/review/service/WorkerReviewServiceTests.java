package com.kuaima.app.domain.review.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.common.ForbiddenBusinessException;
import com.kuaima.app.domain.boss.constant.BossStatus;
import com.kuaima.app.domain.boss.entity.BaseOrderItem;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.review.entity.WorkerReview;
import com.kuaima.app.domain.review.model.WorkerReviewModels.SaveRequest;
import com.kuaima.app.domain.review.repository.WorkerReviewRepository;
import com.kuaima.app.domain.user.service.CreditScoreService;

class WorkerReviewServiceTests {
    private WorkerReviewRepository reviews;
    private BossOrderRespository orders;
    private BaseOrderItemRespository items;
    private CreditScoreService scores;
    private WorkerReviewService service;

    @BeforeEach
    void setUp() {
        reviews = mock(WorkerReviewRepository.class);
        orders = mock(BossOrderRespository.class);
        items = mock(BaseOrderItemRespository.class);
        scores = mock(CreditScoreService.class);
        service = new WorkerReviewService(reviews, orders, items, scores);
        when(reviews.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void completedOrder_shouldCreateOneReviewPerCompletedWorker() {
        order(10L, 99L, BossStatus.ORDER_COMPLETED);
        when(items.findByOrderId(10L)).thenReturn(List.of(item(1L, 10L, 21L, BossStatus.ITEM_FINISHED),
                item(2L, 10L, 22L, BossStatus.ITEM_FINISHED), item(3L, 10L, 23L, BossStatus.ITEM_ON_WORK)));
        when(reviews.existsByItemId(any())).thenReturn(false);

        var result = service.saveOrderReviews(99L, 10L, new SaveRequest(5, 4, 5, 4, "  做得很好  "));

        assertNotNull(result);
        verify(reviews, times(2)).save(any(WorkerReview.class));
        verify(scores).adjust(21L, CreditScoreService.WORKER_STAR, 100, "WORKER_FIVE_STAR_REVIEW", "WORKER_REVIEW", "WORKER_FIVE_STAR_REVIEW:1", "获得老板五星好评");
        verify(scores).adjust(22L, CreditScoreService.WORKER_STAR, 100, "WORKER_FIVE_STAR_REVIEW", "WORKER_REVIEW", "WORKER_FIVE_STAR_REVIEW:2", "获得老板五星好评");
    }

    @Test
    void repeatedSubmit_shouldNotCreateOrAddScoreAgain() {
        order(10L, 99L, BossStatus.ORDER_COMPLETED);
        when(items.findByOrderId(10L)).thenReturn(List.of(item(1L, 10L, 21L, BossStatus.ITEM_FINISHED)));
        when(reviews.existsByItemId(1L)).thenReturn(true);

        assertTrue(service.saveOrderReviews(99L, 10L, new SaveRequest(5, 5, 5, 5, null)).isEmpty());
        verify(reviews, never()).save(any());
        verifyNoInteractions(scores);
    }

    @Test
    void nonOwnerAndIncompleteOrder_shouldBeRejected() {
        order(10L, 99L, BossStatus.ORDER_COMPLETED);
        when(items.findByOrderId(10L)).thenReturn(List.of(item(1L, 10L, 21L, BossStatus.ITEM_FINISHED)));
        assertThrows(ForbiddenBusinessException.class,
                () -> service.saveOrderReviews(88L, 10L, new SaveRequest(5, 5, 5, 5, null)));

        order(11L, 99L, BossStatus.ORDER_RECRUIT_END);
        assertThrows(IllegalStateException.class,
                () -> service.saveOrderReviews(99L, 11L, new SaveRequest(5, 5, 5, 5, null)));
    }

    @Test
    void invalidScoresAndLongContent_shouldBeRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveOrderReviews(99L, 10L, new SaveRequest(0, 5, 5, 5, null)));
        assertThrows(IllegalArgumentException.class,
                () -> service.saveOrderReviews(99L, 10L, new SaveRequest(5, 5, 5, 5, "x".repeat(201))));
    }

    private void order(Long id, Long bossId, String status) {
        BossOrder order = new BossOrder();
        order.setId(id); order.setCreateBy(bossId); order.setOrderStatus(status);
        when(orders.findById(id)).thenReturn(Optional.of(order));
    }

    private BaseOrderItem item(Long id, Long orderId, Long workerId, String status) {
        BaseOrderItem item = new BaseOrderItem();
        item.setId(id); item.setOrderId(orderId); item.setUserId(workerId); item.setStatus(status);
        return item;
    }
}

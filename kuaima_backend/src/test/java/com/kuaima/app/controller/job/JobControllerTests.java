package com.kuaima.app.controller.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.entity.BossOrder;
import com.kuaima.app.domain.boss.service.BossOrderService;
import com.kuaima.app.domain.browsehistory.entity.BrowseHistory;
import com.kuaima.app.domain.browsehistory.repository.BrowseHistoryRepository;
import com.kuaima.app.domain.jobfavorite.repository.JobFavoriteRepository;
import com.kuaima.app.domain.enterprise.entity.Enterprise;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;

class JobControllerTests {

    @Test
    void recordBrowse_shouldAcceptJsonIntegerValues() {
        BrowseHistoryRepository historyRepository = mock(BrowseHistoryRepository.class);
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        JobController controller = new JobController(
                mock(JobFavoriteRepository.class), historyRepository,
                mock(BossOrderRespository.class), mock(BossOrderService.class));
        Map<String, Object> body = new HashMap<>();
        body.put("userId", Integer.valueOf(2));
        body.put("orderId", Integer.valueOf(19));

        BrowseHistory saved = controller.recordBrowse(body).getData();

        assertEquals(2L, saved.getUserId());
        assertEquals(19L, saved.getOrderId());
    }

    @Test
    void publicJobDetail_shouldReturnEnterpriseCompanyName() {
        BossOrderRespository orders = mock(BossOrderRespository.class);
        EnterpriseRepository enterprises = mock(EnterpriseRepository.class);
        JobController controller = new JobController(mock(JobFavoriteRepository.class),
                mock(BrowseHistoryRepository.class), orders, mock(BossOrderService.class), enterprises);
        BossOrder order = new BossOrder();
        order.setId(9L);
        order.setOrderStatus("招工中");
        order.setEnterpriseId(3L);
        order.setLongitude(new java.math.BigDecimal("121.473701"));
        order.setLatitude(new java.math.BigDecimal("31.230416"));
        Enterprise enterprise = new Enterprise();
        enterprise.setId(3L);
        enterprise.setCompanyName("快马科技");
        when(orders.findById(9L)).thenReturn(java.util.Optional.of(order));
        when(enterprises.findById(3L)).thenReturn(java.util.Optional.of(enterprise));

        var result = controller.getPublicJob(9L);

        assertEquals("快马科技", result.getData().getCompanyName());
        assertEquals(new java.math.BigDecimal("121.473701"), result.getData().getLongitude());
        assertEquals(new java.math.BigDecimal("31.230416"), result.getData().getLatitude());
    }
}

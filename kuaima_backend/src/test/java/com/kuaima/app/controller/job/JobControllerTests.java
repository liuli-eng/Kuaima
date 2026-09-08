package com.kuaima.app.controller.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.boss.repository.BossOrderRespository;
import com.kuaima.app.domain.boss.service.BossOrderService;
import com.kuaima.app.domain.browsehistory.entity.BrowseHistory;
import com.kuaima.app.domain.browsehistory.repository.BrowseHistoryRepository;
import com.kuaima.app.domain.jobfavorite.repository.JobFavoriteRepository;

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
}

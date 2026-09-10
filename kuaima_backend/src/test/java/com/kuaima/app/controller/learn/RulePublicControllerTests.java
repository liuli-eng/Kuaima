package com.kuaima.app.controller.learn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;

class RulePublicControllerTests {

    private static final List<String> PUBLISHED_STATUSES = List.of("已发布", "published");

    @Test
    void listRules_shouldReturnChineseAndWebPublishedStatuses() {
        RulesRepository repository = mock(RulesRepository.class);
        RulePublicController controller = new RulePublicController(repository);
        Rules chinesePublished = rule(1L, "已发布");
        Rules webPublished = rule(2L, "published");
        when(repository.findByStatusIn(PUBLISHED_STATUSES))
                .thenReturn(List.of(chinesePublished, webPublished));

        var result = controller.listRules(null);

        assertEquals(List.of(chinesePublished, webPublished), result.getData());
        verify(repository).findByStatusIn(PUBLISHED_STATUSES);
    }

    @Test
    void listRules_shouldApplyCategoryWithBothPublishedStatuses() {
        RulesRepository repository = mock(RulesRepository.class);
        RulePublicController controller = new RulePublicController(repository);
        Rules published = rule(2L, "published");
        when(repository.findByStatusInAndCategory(PUBLISHED_STATUSES, "交易规则"))
                .thenReturn(List.of(published));

        var result = controller.listRules("交易规则");

        assertEquals(List.of(published), result.getData());
        verify(repository).findByStatusInAndCategory(PUBLISHED_STATUSES, "交易规则");
    }

    private Rules rule(Long id, String status) {
        Rules rule = new Rules();
        rule.setId(id);
        rule.setStatus(status);
        return rule;
    }
}

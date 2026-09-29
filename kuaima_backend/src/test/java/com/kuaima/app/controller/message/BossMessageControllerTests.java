package com.kuaima.app.controller.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import com.kuaima.app.common.Result;
import com.kuaima.app.admin.entity.Notice;
import com.kuaima.app.admin.repository.NoticeRepository;
import com.kuaima.app.domain.message.repository.MessageRepository;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.domain.user.service.CertificationService;
import com.kuaima.app.domain.wallet.repository.SettlementRespository;
import com.kuaima.app.security.model.LoginUser;

class BossMessageControllerTests {

    @Test
    void summary_shouldOnlyReturnCardsBackedByCurrentStateOrMessages() {
        MessageRepository messages = mock(MessageRepository.class);
        CertificationService certifications = mock(CertificationService.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        NoticeRepository notices = mock(NoticeRepository.class);
        BossMessageController controller = new BossMessageController(messages, certifications, settlements, notices);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(7L, "boss", UserRole.BOSS));
        when(certifications.publishEligibility(7L)).thenReturn(Map.of(
                "enterpriseStatus", "APPROVED", "realnameStatus", "APPROVED", "canPublish", true, "missing", List.of()));
        when(messages.countByUserIdAndRoleAndReadFlagFalse(7L, UserRole.BOSS)).thenReturn(0L);
        when(messages.countByUserIdAndRoleAndTypeAndReadFlagFalse(7L, UserRole.BOSS, "ORDER_APPLY")).thenReturn(0L);
        when(messages.findByUserIdAndRoleAndTypeOrderByIdDesc(7L, UserRole.BOSS, "ORDER_APPLY", PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));
        when(messages.findFirstByUserIdAndRoleAndTypeOrderByIdDesc(7L, UserRole.BOSS, "SYSTEM_NOTICE"))
                .thenReturn(java.util.Optional.empty());
        when(notices.findFirstByStatusAndScopeInOrderByIdDesc("已发布", List.of("全部", "雇主", "老板")))
                .thenReturn(java.util.Optional.empty());
        when(messages.findFirstByUserIdAndRoleAndTypeOrderByIdDesc(7L, UserRole.BOSS, "ORDER_APPLY"))
                .thenReturn(java.util.Optional.empty());
        when(settlements.findByBossId(7L, PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));

        Result<?> result = controller.summary(authentication);

        var summary = (com.kuaima.app.domain.message.model.BossMessageModels.Summary) result.getData();
        assertEquals(0, summary.items().size());
        assertEquals(false, summary.enterpriseCertUnread());
    }

    @Test
    void summary_shouldUseLatestPublishedBossNotice() {
        MessageRepository messages = mock(MessageRepository.class);
        CertificationService certifications = mock(CertificationService.class);
        SettlementRespository settlements = mock(SettlementRespository.class);
        NoticeRepository notices = mock(NoticeRepository.class);
        BossMessageController controller = new BossMessageController(messages, certifications, settlements, notices);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(7L, "boss", UserRole.BOSS));
        when(certifications.publishEligibility(7L)).thenReturn(Map.of(
                "enterpriseStatus", "APPROVED", "realnameStatus", "APPROVED", "canPublish", true, "missing", List.of()));
        when(messages.findByUserIdAndRoleAndTypeOrderByIdDesc(7L, UserRole.BOSS, "ORDER_APPLY", PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));
        when(messages.findFirstByUserIdAndRoleAndTypeOrderByIdDesc(7L, UserRole.BOSS, "SYSTEM_NOTICE"))
                .thenReturn(java.util.Optional.empty());
        when(messages.findFirstByUserIdAndRoleAndTypeOrderByIdDesc(7L, UserRole.BOSS, "ORDER_APPLY"))
                .thenReturn(java.util.Optional.empty());
        when(settlements.findByBossId(7L, PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));
        Notice notice = new Notice();
        notice.setId(19L);
        notice.setTitle("老板公告");
        notice.setContent("公告内容");
        notice.setScope("雇主");
        notice.setStatus("已发布");
        notice.setPublishTime(java.time.LocalDateTime.of(2026, 9, 28, 10, 0));
        when(notices.findFirstByStatusAndScopeInOrderByIdDesc("已发布", List.of("全部", "雇主", "老板")))
                .thenReturn(java.util.Optional.of(notice));

        var summary = controller.summary(authentication).getData();

        assertEquals("老板公告", summary.latestSystemNotice().title());
        assertEquals(19L, summary.latestSystemNotice().bizId());
        assertEquals(1L, summary.unreadCount());
        assertEquals("公告内容", summary.items().get(0).content());
        assertEquals("system-notice", summary.items().get(0).key());
    }
}

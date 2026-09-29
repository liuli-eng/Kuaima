package com.kuaima.app.controller.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import com.kuaima.app.admin.entity.Notice;
import com.kuaima.app.admin.repository.NoticeRepository;
import com.kuaima.app.domain.message.service.MessageService;
import com.kuaima.app.domain.user.constant.UserRole;
import com.kuaima.app.security.model.LoginUser;

class MessageControllerNoticeTests {

    @Test
    void system_shouldReturnPublishedBossNotices() {
        MessageService messages = mock(MessageService.class);
        NoticeRepository notices = mock(NoticeRepository.class);
        MessageController controller = new MessageController(messages, notices);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new LoginUser(7L, "boss", UserRole.BOSS));
        Notice notice = new Notice();
        notice.setId(19L);
        notice.setTitle("老板公告");
        notice.setScope("雇主");
        notice.setStatus("已发布");
        when(notices.findByStatusAndScopeInOrderByIdDesc(
                "已发布", List.of("全部", "雇主", "老板"), PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(notice), PageRequest.of(0, 20), 1));

        var result = controller.system(7L, "BOSS", null, 0, 20, authentication);

        assertEquals(1, result.getTotal());
        assertEquals(notice, result.getData().get(0));
    }
}

package com.kuaima.app.domain.enterprise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kuaima.app.domain.enterprise.entity.Enterprise;
import com.kuaima.app.domain.enterprise.entity.EnterpriseMember;
import com.kuaima.app.domain.enterprise.repository.EnterpriseMemberRepository;
import com.kuaima.app.domain.enterprise.repository.EnterpriseRepository;
import com.kuaima.app.domain.user.constant.CertificationStatus;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.security.model.LoginUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class EnterpriseContextServiceTests {

    @Test
    void require_shouldRepairOwnerMembershipForPreviouslyCertifiedBoss() {
        UserRepository users = mock(UserRepository.class);
        EnterpriseRepository enterprises = mock(EnterpriseRepository.class);
        EnterpriseMemberRepository members = mock(EnterpriseMemberRepository.class);
        EnterpriseContextService service = new EnterpriseContextService(users, enterprises, members);
        User user = new User();
        user.setId(7L);
        user.setUsername("boss-7");
        user.setEnterpriseStatus(CertificationStatus.APPROVED);
        user.setCompanyCode("E000007");
        user.setCompanyName("快马测试企业");
        Enterprise enterprise = new Enterprise();
        enterprise.setId(17L);
        enterprise.setCompanyCode("E000007");
        enterprise.setCompanyName("快马测试企业");
        enterprise.setStatus("ACTIVE");
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(members.findFirstByUserIdAndStatusOrderByIdAsc(7L, "ACTIVE")).thenReturn(Optional.empty());
        when(enterprises.findByCompanyCode("E000007")).thenReturn(Optional.of(enterprise));
        when(enterprises.findById(17L)).thenReturn(Optional.of(enterprise));
        when(members.findByEnterpriseIdAndUserId(17L, 7L)).thenReturn(Optional.empty());
        when(members.save(org.mockito.ArgumentMatchers.any(EnterpriseMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        var authentication = new UsernamePasswordAuthenticationToken(
                new LoginUser(7L, "boss-7", "BOSS"), null);

        EnterpriseContextService.Context context = service.require(authentication);

        assertSame(enterprise, context.enterprise());
        assertEquals(7L, context.member().getUserId());
        assertEquals(17L, context.member().getEnterpriseId());
        assertEquals("OWNER", context.member().getMemberRole());
        assertEquals("ACTIVE", context.member().getStatus());
        verify(members).save(context.member());
    }
}

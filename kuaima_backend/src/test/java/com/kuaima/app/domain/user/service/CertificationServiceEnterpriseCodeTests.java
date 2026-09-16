package com.kuaima.app.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.kuaima.app.admin.entity.Certification;
import com.kuaima.app.admin.repository.CertificationRepository;
import com.kuaima.app.domain.user.constant.CertificationStatus;
import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CertificationServiceEnterpriseCodeTests {
    @Test
    void approvingEnterpriseCreatesCompanyCode() {
        UserRepository users = mock(UserRepository.class);
        CertificationRepository certifications = mock(CertificationRepository.class);
        CertificationService service = new CertificationService(users, certifications);
        Certification record = new Certification();
        record.setId(10L); record.setUserId(43L); record.setType("ENTERPRISE");
        User user = new User(); user.setId(43L);
        when(certifications.findById(10L)).thenReturn(Optional.of(record));
        when(users.findById(43L)).thenReturn(Optional.of(user));
        when(certifications.save(record)).thenReturn(record);

        service.audit(10L, true, null);

        assertEquals(CertificationStatus.APPROVED, user.getEnterpriseStatus());
        assertEquals("ENT00000043", user.getCompanyCode());
        verify(users).save(user);
    }

    @Test
    void reapprovalKeepsExistingCompanyCode() {
        UserRepository users = mock(UserRepository.class);
        CertificationRepository certifications = mock(CertificationRepository.class);
        CertificationService service = new CertificationService(users, certifications);
        Certification record = new Certification();
        record.setId(11L); record.setUserId(43L); record.setType("企业认证");
        User user = new User(); user.setId(43L); user.setCompanyCode("ENT-CUSTOM-001");
        when(certifications.findById(11L)).thenReturn(Optional.of(record));
        when(users.findById(43L)).thenReturn(Optional.of(user));
        when(certifications.save(record)).thenReturn(record);

        service.audit(11L, true, null);

        assertEquals("ENT-CUSTOM-001", user.getCompanyCode());
    }
}

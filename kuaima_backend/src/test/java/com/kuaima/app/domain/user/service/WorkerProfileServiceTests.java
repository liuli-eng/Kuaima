package com.kuaima.app.domain.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kuaima.app.domain.user.entity.User;
import com.kuaima.app.domain.user.model.WorkerProfileModels.UpdateWorkerProfileRequest;
import com.kuaima.app.domain.user.repository.UserRepository;
import com.kuaima.app.domain.boss.repository.BaseOrderItemRespository;
import com.kuaima.app.domain.boss.repository.BossOrderRespository;

class WorkerProfileServiceTests {

    private UserRepository userRepository;
    private WorkerProfileService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new WorkerProfileService(userRepository,
                mock(BaseOrderItemRespository.class), mock(BossOrderRespository.class));
    }

    @Test
    void updateProfile_shouldPersistPrototypeFieldsAndKeepPhoneReadOnly() {
        User user = new User();
        user.setId(30L);
        user.setPhone("13800005678");
        when(userRepository.findById(30L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateWorkerProfileRequest request = new UpdateWorkerProfileRequest(
                "avatar.png", "海绵宝宝", "女", LocalDate.of(1990, 1, 1),
                "上海市浦东新区", "分拣打包,搬运装卸", 3, true, "介绍一下自己");

        var result = service.updateProfile(30L, request);

        assertEquals("海绵宝宝", result.nickname());
        assertEquals("13800005678", result.phone());
        assertEquals(LocalDate.of(1990, 1, 1), result.birthday());
        assertEquals(3, result.workYears());
        assertEquals(true, result.acceptNightShift());
        assertEquals("介绍一下自己", result.introduction());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldRejectFutureBirthdayAndInvalidWorkYears() {
        assertThrows(IllegalArgumentException.class, () -> service.updateProfile(30L,
                new UpdateWorkerProfileRequest(null, null, null, LocalDate.now().plusDays(1),
                        null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.updateProfile(30L,
                new UpdateWorkerProfileRequest(null, null, null, null,
                        null, null, 81, null, null)));
    }
}

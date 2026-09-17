package com.kuaima.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SmsServiceTests {
    @Test
    void sendCode_shouldEnforceMainlandPhoneResendIntervalAndDailyLimit() {
        SmsService service = new SmsService();
        ReflectionTestUtils.setField(service, "mockEnabled", true);
        ReflectionTestUtils.setField(service, "mockCode", "123456");
        ReflectionTestUtils.setField(service, "codeExpiration", 300);
        ReflectionTestUtils.setField(service, "resendInterval", 60);
        ReflectionTestUtils.setField(service, "dailyLimit", 10);

        assertEquals("手机号格式不正确", service.sendCode("12800138000"));
        assertNull(service.sendCode("13900131234"));
        assertTrue(service.sendCode("13900131234").contains("发送过于频繁"));
    }

    @Test
    void sendCode_shouldLimitDailySuccessfulSends() {
        SmsService service = new SmsService();
        ReflectionTestUtils.setField(service, "mockEnabled", true);
        ReflectionTestUtils.setField(service, "mockCode", "123456");
        ReflectionTestUtils.setField(service, "codeExpiration", 300);
        // 测试中关闭重发间隔，仅验证每日成功发送上限。
        ReflectionTestUtils.setField(service, "resendInterval", -1);
        ReflectionTestUtils.setField(service, "dailyLimit", 2);

        assertNull(service.sendCode("13900131234"));
        assertNull(service.sendCode("13900131234"));
        assertTrue(service.sendCode("13900131234").contains("今日验证码发送次数已达上限"));
    }

    @Test
    void verifyCodeResult_shouldConsumeCodeOnlyOnceAndDistinguishFailures() {
        SmsService service = new SmsService();
        ReflectionTestUtils.setField(service, "mockEnabled", true);
        ReflectionTestUtils.setField(service, "mockCode", "123456");
        ReflectionTestUtils.setField(service, "codeExpiration", 300);
        ReflectionTestUtils.setField(service, "resendInterval", -1);
        ReflectionTestUtils.setField(service, "dailyLimit", 10);

        assertEquals(SmsService.VerifyResult.NOT_FOUND,
                service.verifyCodeResult("13900131234", "123456"));
        assertNull(service.sendCode("13900131234"));
        assertEquals(SmsService.VerifyResult.INVALID,
                service.verifyCodeResult("13900131234", "654321"));
        assertEquals(SmsService.VerifyResult.OK,
                service.verifyCodeResult("13900131234", "123456"));
        assertEquals(SmsService.VerifyResult.EXPIRED_OR_USED,
                service.verifyCodeResult("13900131234", "123456"));
    }
}

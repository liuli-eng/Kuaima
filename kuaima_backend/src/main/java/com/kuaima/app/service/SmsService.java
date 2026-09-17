package com.kuaima.app.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;

import jakarta.annotation.PostConstruct;

/**
 * 阿里云短信服务
 * - 使用 dysmsapi20170525 SDK 发送验证码短信
 * - 内存存储验证码（ConcurrentHashMap），5分钟过期，60秒重发和每日上限限制
 */
@Service
public class SmsService {

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    @Value("${aliyun.sms.code-expiration:300}")
    private int codeExpiration;

    @Value("${aliyun.sms.resend-interval:60}")
    private int resendInterval;

    /** 同一手机号每个自然日的发送上限，防止验证码接口被刷。 */
    @Value("${aliyun.sms.daily-limit:10}")
    private int dailyLimit;

    /** 开发/测试环境固定验证码开关，生产环境必须保持关闭。 */
    @Value("${aliyun.sms.mock-enabled:false}")
    private boolean mockEnabled;

    @Value("${aliyun.sms.mock-code:000000}")
    private String mockCode;

    private Client client;

    /** 内存验证码存储：phone -> {code, expireAt, sentAt} */
    private final ConcurrentHashMap<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    /** 内存发送次数存储：phone -> {day, count}，仅统计发送成功的验证码。 */
    private final ConcurrentHashMap<String, DailyCount> dailyCountStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint("dysmsapi.aliyuncs.com");
            this.client = new Client(config);
        } catch (Exception e) {
            System.err.println("[SmsService] 初始化阿里云短信客户端失败: " + e.getMessage());
        }
    }

    /**
     * 发送验证码短信
     * @return null=发送成功；非空=错误信息
     */
    public String sendCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return "手机号格式不正确";
        }
        long now = System.currentTimeMillis();
        synchronized (this) {
            CodeEntry existing = codeStore.get(phone);
            if (existing != null && now - existing.sentAt < TimeUnit.SECONDS.toMillis(resendInterval)) {
                long wait = resendInterval - (now - existing.sentAt) / 1000;
                return "发送过于频繁，请" + wait + "秒后重试";
            }
            String today = java.time.LocalDate.now().toString();
            DailyCount dailyCount = dailyCountStore.get(phone);
            if (dailyCount == null || !today.equals(dailyCount.day())) {
                dailyCount = new DailyCount(today, 0);
            }
            if (dailyCount.count() >= dailyLimit) {
                return "今日验证码发送次数已达上限，请明天再试";
            }

            String code = mockEnabled ? mockCode : String.format("%06d", (int) (Math.random() * 1000000));
            if (!mockEnabled) {
                // 调用阿里云发送；发送成功后才计入当日次数并登记验证码。
                try {
                    SendSmsRequest req = new SendSmsRequest()
                            .setPhoneNumbers(phone)
                            .setSignName(signName)
                            .setTemplateCode(templateCode)
                            .setTemplateParam("{\"code\":\"" + code + "\"}");
                    SendSmsResponse resp = client.sendSms(req);
                    if (resp.getBody() == null || !"OK".equals(resp.getBody().getCode())) {
                        String err = resp.getBody() != null ? resp.getBody().getMessage() : "未知错误";
                        System.err.println("[SmsService] 发送失败: " + err);
                        return "短信发送失败: " + err;
                    }
                } catch (Exception e) {
                    System.err.println("[SmsService] 发送异常: " + e.getMessage());
                    return "短信发送异常: " + e.getMessage();
                }
            }
            codeStore.put(phone, new CodeEntry(code, now,
                    now + TimeUnit.SECONDS.toMillis(codeExpiration), null));
            dailyCountStore.put(phone, new DailyCount(today, dailyCount.count() + 1));
            System.out.println("[SmsService] " + (mockEnabled ? "开发环境模拟验证码已登记" : "验证码已发送")
                    + "，phone=" + maskPhone(phone));
            return null;
        }
    }

    /**
     * 校验验证码
     * @return true=验证通过
     */
    public boolean verifyCode(String phone, String code) {
        return verifyCodeResult(phone, code) == VerifyResult.OK;
    }

    /** 原子校验并消费验证码，同时区分不存在/错误，供业务层映射 404/422。 */
    public VerifyResult verifyCodeResult(String phone, String code) {
        if (phone == null || code == null) {
            return VerifyResult.NOT_FOUND;
        }
        VerifyResult[] result = {VerifyResult.NOT_FOUND};
        long now = System.currentTimeMillis();
        codeStore.compute(phone, (key, entry) -> {
            if (entry == null) {
                result[0] = VerifyResult.NOT_FOUND;
                return null;
            }
            if (now > entry.expireAt) {
                result[0] = VerifyResult.EXPIRED_OR_USED;
                return entry;
            }
            if (entry.usedAt != null) {
                result[0] = VerifyResult.EXPIRED_OR_USED;
                return entry;
            }
            if (entry.code.equals(code)) {
                result[0] = VerifyResult.OK;
                return new CodeEntry(entry.code, entry.sentAt, entry.expireAt, now);
            }
            result[0] = VerifyResult.INVALID;
            return entry;
        });
        return result[0];
    }

    public enum VerifyResult { OK, NOT_FOUND, INVALID, EXPIRED_OR_USED }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private record CodeEntry(String code, long sentAt, long expireAt, Long usedAt) {}

    private record DailyCount(String day, int count) {}

    /**
     * 按指定模板发送短信（用于定时批量发送等业务场景）
     * @param phone     手机号
     * @param tplCode   阿里云模板CODE
     * @param paramJson 模板参数 JSON 字符串，如 {"code":"123456"}
     * @return null=成功；非空=错误信息
     */
    public String sendByTemplate(String phone, String tplCode, String paramJson) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            return "手机号格式不正确";
        }
        if (tplCode == null || tplCode.isEmpty()) {
            return "模板CODE不能为空";
        }
        try {
            SendSmsRequest req = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(tplCode)
                    .setTemplateParam(paramJson != null ? paramJson : "{}");
            SendSmsResponse resp = client.sendSms(req);
            if (resp.getBody() != null && "OK".equals(resp.getBody().getCode())) {
                return null;
            } else {
                String err = resp.getBody() != null ? resp.getBody().getMessage() : "未知错误";
                System.err.println("[SmsService] 模板发送失败: " + err);
                return "短信发送失败: " + err;
            }
        } catch (Exception e) {
            System.err.println("[SmsService] 模板发送异常: " + e.getMessage());
            return "短信发送异常: " + e.getMessage();
        }
    }
}

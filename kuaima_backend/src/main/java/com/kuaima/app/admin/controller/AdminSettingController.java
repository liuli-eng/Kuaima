package com.kuaima.app.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.admin.entity.AdminSetting;
import com.kuaima.app.admin.repository.AdminSettingRepository;
import com.kuaima.app.common.Result;

import java.time.LocalDateTime;
import java.util.Map;

/** 系统设置 */
@RestController
@RequestMapping("/admin/settings")
public class AdminSettingController {

    private final AdminSettingRepository repo;

    public AdminSettingController(AdminSettingRepository repo) { this.repo = repo; }

    @GetMapping
    public Result<List<AdminSetting>> list() { return Result.success(repo.findAll()); }

    @GetMapping("/category/{category}")
    public Result<List<AdminSetting>> byCategory(@PathVariable String category) {
        return Result.success(repo.findByCategory(category));
    }

    @GetMapping("/{key}")
    public Result<AdminSetting> get(@PathVariable String key) {
        return Result.success(repo.findById(key).orElseThrow());
    }

    /** 保存/更新 设置 */
    @PutMapping("/{key}")
    public Result<AdminSetting> save(@PathVariable String key, @RequestBody AdminSetting setting) {
        setting.setSettingKey(key);
        return Result.success(repo.save(setting));
    }

    // ==================== 银行账户设置 ====================
    private static final String BANK_ACCOUNT_KEY = "bank_account_info";
    private static final String DEFAULT_BANK_INFO = "{\"bankName\":\"中国工商银行\",\"cardNumber\":\"6222 **** **** 8888\",\"holder\":\"快马日结科技有限公司\",\"branch\":\"北京海淀支行\",\"swiftCode\":\"ICBKCNBJ\",\"bankCode\":\"102100000458\",\"accountType\":\"对公账户\"}";

    /** 获取银行账户信息（返回 JSON 字符串，前端解析） */
    @GetMapping("/bank-account")
    public Result<String> getBankAccount() {
        String value = repo.findById(BANK_ACCOUNT_KEY)
                .map(AdminSetting::getSettingValue)
                .orElse(DEFAULT_BANK_INFO);
        return Result.success(value);
    }

    /** 保存银行账户信息（接收 JSON 字符串） */
    @PutMapping("/bank-account")
    public Result<String> saveBankAccount(@RequestBody String json) {
        try {
            AdminSetting setting = repo.findById(BANK_ACCOUNT_KEY).orElseGet(AdminSetting::new);
            setting.setSettingKey(BANK_ACCOUNT_KEY);
            setting.setCategory("platform");
            setting.setSettingValue(json);
            setting.setDescription("平台收款银行账户信息");
            repo.save(setting);
            return Result.success(json);
        } catch (Exception e) {
            return Result.error("保存银行账户信息失败: " + e.getMessage());
        }
    }

    // ==================== 钱包账户设置 ====================
    private static final String WALLET_ACCOUNT_KEY = "wallet_account_info";
    private static final String DEFAULT_WALLET_INFO = "{\"alipay\":{\"account\":\"kuaima@163.com\",\"holder\":\"快马日结科技有限公司\",\"isDefault\":true},\"wechat\":{\"account\":\"k_m_riji001\",\"holder\":\"快马日结科技有限公司\",\"isDefault\":false}}";

    /** 获取钱包账户信息（返回 JSON 字符串，前端解析） */
    @GetMapping("/wallet-account")
    public Result<String> getWalletAccount() {
        String value = repo.findById(WALLET_ACCOUNT_KEY)
                .map(AdminSetting::getSettingValue)
                .orElse(DEFAULT_WALLET_INFO);
        return Result.success(value);
    }

    /** 保存钱包账户信息（接收 JSON 字符串） */
    @PutMapping("/wallet-account")
    public Result<String> saveWalletAccount(@RequestBody String json) {
        try {
            AdminSetting setting = repo.findById(WALLET_ACCOUNT_KEY).orElseGet(AdminSetting::new);
            setting.setSettingKey(WALLET_ACCOUNT_KEY);
            setting.setCategory("platform");
            setting.setSettingValue(json);
            setting.setDescription("平台收款钱包账户信息");
            repo.save(setting);
            return Result.success(json);
        } catch (Exception e) {
            return Result.error("保存钱包账户信息失败: " + e.getMessage());
        }
    }

    // ==================== 模板测试发送 ====================

    /**
     * 发送模板测试消息
     * 请求体: JSON 字符串 { "type":"sms", "templateTitle":"...", "receiver":"...", "content":"..." }
     * 使用原始 String 接收，避免依赖 Jackson 反序列化
     */
    @PostMapping("/test-send")
    public Result<Map<String, Object>> testSend(@RequestBody String json) {
        String type = extractJsonField(json, "type", "sms");
        String receiver = extractJsonField(json, "receiver", "");
        String content = extractJsonField(json, "content", "");
        String templateTitle = extractJsonField(json, "templateTitle", "");

        if (receiver.isEmpty() || !receiver.matches("^1[3-9]\\d{9}$")) {
            return Result.error("请输入正确的11位手机号");
        }

        // 原型阶段：记录日志并返回成功（无真实短信/推送通道）
        System.out.println("[测试发送] type=" + type + ", receiver=" + receiver
                + ", template=" + templateTitle + ", content=" + content);

        String masked = receiver.substring(0, 3) + "****" + receiver.substring(7);
        return Result.success(Map.of(
                "success", true,
                "maskedReceiver", masked,
                "sentAt", LocalDateTime.now().toString()
        ));
    }

    /** 从简单 JSON 字符串中提取字段值（仅支持字符串值） */
    private String extractJsonField(String json, String key, String defaultValue) {
        if (json == null || json.isEmpty()) return defaultValue;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return defaultValue;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return defaultValue;
        // 跳过空白
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return defaultValue;
        if (json.charAt(start) != '"') return defaultValue;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return defaultValue;
        return json.substring(start + 1, end);
    }
}

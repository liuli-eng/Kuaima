package com.kuaima.app.wechat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {

    /** 微信小程序 AppID */
    private String appid;

    /** 微信小程序 AppSecret */
    private String secret;

    /** 开发/测试环境 Mock 模式：跳过微信 API 验证，直接返回模拟 openid */
    private boolean mockEnabled = false;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }
}

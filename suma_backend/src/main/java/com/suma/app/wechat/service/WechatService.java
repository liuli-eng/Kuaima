package com.suma.app.wechat.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.suma.app.wechat.config.WechatProperties;

@Service
public class WechatService {

    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String GET_PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

    private final WechatProperties properties;
    private final HttpClient httpClient;

    /** access_token 缓存（微信有效期 2 小时，提前 5 分钟过期） */
    private volatile String cachedAccessToken;
    private volatile long accessTokenExpireAt;

    public WechatService(WechatProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /** 小程序登录：js_code -> openid + session_key（昵称头像由前端传入） */
    public WechatUserInfo loginByCode(String code) {
        String url = JSCODE2SESSION_URL
                + "?appid=" + enc(properties.getAppid())
                + "&secret=" + enc(properties.getSecret())
                + "&js_code=" + enc(code)
                + "&grant_type=authorization_code";
        JSONObject resp = get(url);
        checkError(resp, "小程序 code 换取 session 失败");
        return new WechatUserInfo(
                resp.getString("openid"),
                resp.getString("unionid"),
                null,
                null);
    }

    /** 小程序手机号快速验证：getPhoneNumber 按钮回调的 code -> 手机号 */
    public String getPhoneNumber(String phoneCode) {
        String accessToken = getAccessToken();
        String url = GET_PHONE_URL + "?access_token=" + enc(accessToken);
        JSONObject body = new JSONObject();
        body.put("code", phoneCode);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject resp = JSONObject.parseObject(response.body());
            checkError(resp, "手机号换取失败");
            JSONObject phoneInfo = resp.getJSONObject("phone_info");
            return phoneInfo != null ? phoneInfo.getString("phoneNumber") : null;
        } catch (Exception e) {
            throw new RuntimeException("调用微信接口失败: " + e.getMessage(), e);
        }
    }

    /** 获取全局 access_token（带内存缓存） */
    private String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < accessTokenExpireAt) {
            return cachedAccessToken;
        }
        String url = ACCESS_TOKEN_URL
                + "?grant_type=client_credential"
                + "&appid=" + enc(properties.getAppid())
                + "&secret=" + enc(properties.getSecret());
        JSONObject resp = get(url);
        checkError(resp, "获取 access_token 失败");
        cachedAccessToken = resp.getString("access_token");
        long expiresIn = resp.getLongValue("expires_in", 7200L);
        accessTokenExpireAt = System.currentTimeMillis() + (expiresIn - 300) * 1000;
        return cachedAccessToken;
    }

    private JSONObject get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return JSONObject.parseObject(response.body());
        } catch (Exception e) {
            throw new RuntimeException("调用微信接口失败: " + e.getMessage(), e);
        }
    }

    private void checkError(JSONObject json, String action) {
        Integer errcode = json.getInteger("errcode");
        if (errcode != null && errcode != 0) {
            throw new RuntimeException(action + "，errcode=" + errcode + "，errmsg=" + json.getString("errmsg"));
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** 微信用户信息 */
    public record WechatUserInfo(String openid, String unionid, String nickname, String avatar) {
    }
}

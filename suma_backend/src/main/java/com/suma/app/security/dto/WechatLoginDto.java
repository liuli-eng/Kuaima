package com.suma.app.security.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WechatLoginDto {

    /** 前端从小程序 wx.login() 获取的 code（必填） */
    private String code;

    /** 昵称（可选，通过微信头像昵称填写能力获取后由前端传入） */
    private String nickname;

    /** 头像 URL（可选） */
    private String avatar;

    /** 手机号动态令牌（可选，来自 open-type="getPhoneNumber" 按钮回调的 code） */
    private String phoneCode;

    /** 登录选择的身份：BOSS(老板) / USER(员工)；缺省按 USER(员工) 处理，非法值报 400 */
    private String role;
}

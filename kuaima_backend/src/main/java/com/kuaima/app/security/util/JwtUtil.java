package com.kuaima.app.security.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_WECHAT_REGISTRATION = "wechat_registration";

    private static final long WECHAT_REGISTRATION_EXPIRATION = 5 * 60 * 1000L;

    private final SecretKey key;
    private final long accessExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration:1800000}") long accessExpiration) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpiration = accessExpiration;
    }

    public String generateAccessToken(String username, String role, Long uid) {
        Date now = new Date();
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("type", TYPE_ACCESS)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpiration))
                .signWith(key);
        if (uid != null) {
            builder.claim("uid", uid);
        }
        return builder.compact();
    }

    /** 新微信用户完成手机号授权前使用的短期凭证。 */
    public String generateWechatRegistrationToken(String openid) {
        Date now = new Date();
        return Jwts.builder()
                .subject(openid)
                .claim("type", TYPE_WECHAT_REGISTRATION)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + WECHAT_REGISTRATION_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public String getWechatRegistrationOpenid(String token) {
        Claims claims = parseClaims(token);
        Object type = claims.get("type");
        if (!TYPE_WECHAT_REGISTRATION.equals(type == null ? null : type.toString())) {
            throw new IllegalArgumentException("注册凭证类型无效");
        }
        return claims.getSubject();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** token 载荷中的用户ID（uid claim），缺失时返回 null */
    public Long getUserId(String token) {
        Object uid = parseClaims(token).get("uid");
        if (uid instanceof Number number) {
            return number.longValue();
        }
        if (uid != null) {
            return Long.parseLong(uid.toString());
        }
        return null;
    }

    public String getRole(String token) {
        Object role = parseClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    public boolean isAccessToken(String token) {
        return isValid(token) && TYPE_ACCESS.equals(getTokenType(token));
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String getTokenType(String token) {
        Object type = parseClaims(token).get("type");
        return type == null ? null : type.toString();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

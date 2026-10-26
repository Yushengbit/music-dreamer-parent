package com.musicdreamer.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具类：基于 jjwt 0.12.x
 * 负责 token 的签发与解析
 */
@Component
public class JwtUtil {

    // 提供默认值，避免未配置该属性的服务（如 music-user）启动失败
    @Value("${music.jwt.secret:music-dreamer-jwt-secret-key-must-be-at-least-32bytes-long-2026}")
    private String secret;

    @Value("${music.jwt.expiration:7200}")
    private Long expiration;

    private SecretKey secretKey;

    @PostConstruct//被PostConstruct 被该方法注解的会靠前执行
    public void init() {
        // HMAC-SHA 密钥至少需要 32 字节（256 位）
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 签发 token
     */
    public String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration * 1000);
        return Jwts.builder()
                // jti：token 唯一标识，登出黑名单以它为 key
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 token，获取 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 token 中获取用户 id
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public Long getExpiration() {
        return expiration;
    }
}
package com.musicdreamer.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 网关专用的 JwtUtil
 *
 * <p>为什么不直接引入 music-common？
 * music-common 依赖了 spring-boot-starter-web，会与 gateway 的 webflux 冲突，
 * 故在网关内复制一份，内容与 music-common 保持一致，密钥靠 application.yml 同步。
 *
 * <p>⚠️ 修改此文件时，请同时同步修改 music-common 里的 JwtUtil
 */
@Component
public class JwtUtil {

    @Value("${music.jwt.secret:music-dreamer-jwt-secret-key-must-be-at-least-32bytes-long-2026}")
    private String secret;

    @Value("${music.jwt.expiration:7200}")
    private Long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
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

    public Long getExpiration() {
        return expiration;
    }
}
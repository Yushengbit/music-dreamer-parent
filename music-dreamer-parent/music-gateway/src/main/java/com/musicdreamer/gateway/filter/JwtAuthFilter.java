package com.musicdreamer.gateway.filter;

import com.musicdreamer.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关全局 JWT 鉴权过滤器
 *
 * <p>职责：
 * <ol>
 *   <li>白名单路径直接放行（如 /auth/login、/auth/register）</li>
 *   <li>其他路径必须携带 Authorization: Bearer xxx</li>
 *   <li>验签 + 校验是否过期</li>
 *   <li>查 Redis 黑名单（登出的 token 会被 auth 写入），命中则 401</li>
 *   <li>校验通过后，把 userId/role 写入请求头 X-User-* 传给下游</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * 白名单：从 application.yml 的 music.gateway.auth.white-list 读取
     */
    @Value("${music.gateway.auth.white-list:/auth/login,/auth/register}")
    private List<String> whiteList;

    /**
     * 黑名单 key 前缀，必须与 music-auth 的 music.jwt.blacklist-prefix 保持一致
     */
    @Value("${music.jwt.blacklist-prefix:music:black:jti:}")
    private String blacklistPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1) 白名单直接放行
        if (isWhiteList(path)) {
            log.debug("[Gateway] 白名单放行: {}", path);
            return chain.filter(exchange);
        }

        // 2) 取 Authorization 头
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[Gateway] 缺少 Authorization 头: {}", path);
            return unauthorized(exchange, "请先登录");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange, "token 不能为空");
        }

        // 3) 解析 token（验签 + 过期）
        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("[Gateway] token 解析失败: path={}, err={}", path, e.getMessage());
            return unauthorized(exchange, "token 无效或已过期");
        }

        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            return unauthorized(exchange, "token 缺少 jti");
        }

        // 4) 查黑名单：登出的 token 会被 auth 写入 Redis
        return redisTemplate.hasKey(blacklistPrefix + jti)
                .defaultIfEmpty(false)
                // Redis 异常时「失败关闭」：宁可拒绝也不放行，保证安全
                .onErrorResume(e -> {
                    log.error("[Gateway] 查询 Redis 黑名单异常，拒绝访问: {}", e.getMessage());
                    return Mono.error(e);
                })
                .flatMap(inBlacklist -> {
                    if (Boolean.TRUE.equals(inBlacklist)) {
                        log.warn("[Gateway] token 已登出（命中黑名单）: jti={}", jti);
                        return unauthorized(exchange, "登录已失效，请重新登录");
                    }
                    return passThrough(exchange, chain, request, claims, path);
                })
                .onErrorResume(e -> unauthorized(exchange, "鉴权服务异常，请稍后再试"));
    }

    /**
     * 校验通过：把用户信息写入请求头传给下游
     */
    private Mono<Void> passThrough(ServerWebExchange exchange,
                                   GatewayFilterChain chain,
                                   ServerHttpRequest request,
                                   Claims claims,
                                   String path) {
        Object userId = claims.getSubject();
        Object username = claims.get("username");
        Object role = claims.get("role");

        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", userId == null ? "" : String.valueOf(userId))
                .header("X-User-Name", username == null ? "" : String.valueOf(username))
                .header("X-User-Role", role == null ? "0" : String.valueOf(role))
                .build();

        log.debug("[Gateway] 鉴权通过: userId={}, path={}", userId, path);
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    /**
     * 判断 path 是否命中白名单
     * 支持 /auth/login 精确匹配 和 /auth/login/xxx 前缀匹配
     */
    private boolean isWhiteList(String path) {
        if (whiteList == null || whiteList.isEmpty()) {
            return false;
        }
        for (String w : whiteList) {
            if (path.equals(w) || path.startsWith(w + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 401 JSON 响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"msg\":\"" + escape(msg) + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 简单 JSON 转义，防 XSS
     */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 数字越小优先级越高，让这个过滤器在最前面跑
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
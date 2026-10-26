package com.musicdreamer.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musicdreamer.auth.dto.LoginDTO;
import com.musicdreamer.auth.dto.RegisterDTO;
import com.musicdreamer.auth.entity.User;
import com.musicdreamer.auth.mapper.UserMapper;
import com.musicdreamer.auth.service.AuthService;
import com.musicdreamer.auth.vo.LoginVO;
import com.musicdreamer.common.exception.BusinessException;
import com.musicdreamer.common.exception.GlobalException;
import com.musicdreamer.common.result.ResultCodeEnum;
import com.musicdreamer.common.util.BCryptUtil;
import com.musicdreamer.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    /**
     * 黑名单 key 前缀，必须与网关的 music.jwt.blacklist-prefix 保持一致
     */
    @Value("${music.jwt.blacklist-prefix:music:black:jti:}")
    private String blacklistPrefix;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 1. 校验用户名是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在"+dto.getUsername()+"，请更换其他用户名");
        }

        // 2. 构造 user，密码加密
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCryptUtil.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1);  // 默认启用
        user.setRole(0);    // 默认普通用户

        userMapper.insert(user);
        log.info("用户注册成功：username={}", dto.getUsername());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 查用户（@TableLogic 自动过滤 deleted=1）
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new GlobalException(ResultCodeEnum.USER_NOT_EXIST);
        }

        // 2. 校验密码
        if (!BCryptUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new GlobalException(ResultCodeEnum.USER_PASSWORD_ERROR);
        }

        // 3. 校验状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new GlobalException(ResultCodeEnum.USER_DISABLED);
        }

        // 4. 签发 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 5. 构造返回
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .expiresIn(jwtUtil.getExpiration())
                .build();
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            // token 本身已无效/过期，无需入黑名单
            log.debug("登出时 token 已失效，忽略：{}", e.getMessage());
            return;
        }

        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            log.warn("token 缺少 jti 声明，无法加入黑名单");
            return;
        }

        // TTL = token 剩余有效期，token 自然过期后黑名单记录自动清理，不会无限膨胀
        long ttl = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
        if (ttl <= 0) {
            return;
        }

        redisTemplate.opsForValue().set(blacklistPrefix + jti, "1", ttl, TimeUnit.SECONDS);
        log.info("用户登出，token 已加入黑名单：jti={}, ttl={}s", jti, ttl);
    }
}
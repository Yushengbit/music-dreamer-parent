package com.musicdreamer.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密工具（基于 spring-security-crypto）
 */
//被final修改的类不能被继承，方法不能被重写
public final class BCryptUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private BCryptUtil() {
    }

    /**
     * 加密明文密码
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码与密文是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
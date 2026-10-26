package com.musicdreamer.auth.dto;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginDTO {

    /** 用户名 */
    private String username;

    /** 密码（明文） */
    private String password;
}